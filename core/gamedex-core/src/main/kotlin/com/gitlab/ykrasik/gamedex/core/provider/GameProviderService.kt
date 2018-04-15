/****************************************************************************
 * Copyright (C) 2016-2018 Yevgeny Krasik                                   *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package com.gitlab.ykrasik.gamedex.core.provider

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.ProviderData
import com.gitlab.ykrasik.gamedex.ProviderHeader
import com.gitlab.ykrasik.gamedex.core.api.file.FileSystemService
import com.gitlab.ykrasik.gamedex.core.api.provider.*
import com.gitlab.ykrasik.gamedex.core.game.GameUserConfig
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import com.gitlab.ykrasik.gamedex.provider.GameProvider
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import com.gitlab.ykrasik.gamedex.provider.ProviderSearchResult
import com.gitlab.ykrasik.gamedex.util.now
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 13/10/2016
 * Time: 13:29
 */
// TODO: Make this a presenter.
@Singleton
class GameProviderServiceImpl @Inject constructor(
    private val providerRepository: GameProviderRepository,
    private val fileSystemService: FileSystemService,
    private val userConfigRepository: UserConfigRepository,
    private val chooser: SearchChooser
) : GameProviderService {
    private val gameUserConfig = userConfigRepository[GameUserConfig::class]

    override suspend fun search(taskData: ProviderTaskData, excludedProviders: List<ProviderId>): SearchResults? =
        try {
            SearchContext(taskData, excludedProviders).search()
        } catch (e: CancelSearchException) {
            null
        }

    private inner class SearchContext(
        private val taskData: ProviderTaskData,
        private val excludedProviders: List<ProviderId>
    ) {
        private var searchedName = fileSystemService.fromFileName(fileSystemService.analyzeFolderName(taskData.name).gameName)
        private var canAutoContinue = chooseResults != GameUserConfig.ChooseResults.alwaysChoose
        private val previouslyDiscardedResults = mutableSetOf<ProviderSearchResult>()
        private val newlyExcludedProviders = mutableListOf<ProviderId>()
        private var userExactMatch: String? = null

        private val task get() = taskData.task
        private val platform get() = taskData.platform
        private val chooseResults get() = gameUserConfig.chooseResults

        // TODO: Support a back button somehow, it's needed...
        suspend fun search(): SearchResults {
            val providersToSearch = providerRepository.enabledProviders.filter { shouldSearch(it) }
            val results = task.run {
                providersToSearch.mapNotNullWithProgress {
                    search(it)
                }
            }
            val name = userExactMatch ?: searchedName
            val providerData = download(taskData.copy(name = name), results)
            return SearchResults(providerData, newlyExcludedProviders)
        }

        private fun shouldSearch(provider: GameProvider): Boolean =
            provider.supports(platform) && !excludedProviders.contains(provider.id)

        private suspend fun search(provider: EnabledGameProvider): ProviderHeader? {
            task.message1 = "[${provider.id}] Searching: '$searchedName'..."
            val results = provider.search(searchedName, platform)
            task.message2 = "${results.size} results."

            fun findExactMatch(target: String): ProviderSearchResult? = results.find { it.name.equals(target, ignoreCase = true) }

            val choice = when {
                chooseResults == GameUserConfig.ChooseResults.alwaysChoose -> chooseResult(provider, results)
                userExactMatch != null -> {
                    val providerExactMatch = findExactMatch(userExactMatch!!)
                    if (providerExactMatch != null) {
                        return providerExactMatch.toHeader(provider)
                    } else {
                        chooseResult(provider, results)
                    }
                }
                canAutoContinue && results.size == 1 && findExactMatch(searchedName) != null ->
                    return results.first().toHeader(provider)
                else -> chooseResult(provider, results)
            }

            fun ProviderSearchResult.markChosen(): ProviderHeader {
                previouslyDiscardedResults += results - this
                return this.toHeader(provider)
            }

            return when (choice) {
                is SearchChooser.Choice.ExactMatch -> choice.result.apply { userExactMatch = name; searchedName = name }.markChosen()
                is SearchChooser.Choice.NotExactMatch -> choice.result.markChosen()
                is SearchChooser.Choice.NewSearch -> {
                    searchedName = choice.newSearch.trim()
                    canAutoContinue = false
                    search(provider)
                }
                is SearchChooser.Choice.ExcludeProvider -> {
                    newlyExcludedProviders += provider.id
                    null
                }
                SearchChooser.Choice.ProceedWithout -> null
                SearchChooser.Choice.Cancel -> throw CancelSearchException()
            }
        }

        private suspend fun chooseResult(provider: GameProvider, allSearchResults: List<ProviderSearchResult>): SearchChooser.Choice {
            // We only get here when we have no exact matches.
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (chooseResults) {
                GameUserConfig.ChooseResults.skipIfNonExact -> return SearchChooser.Choice.Cancel
                GameUserConfig.ChooseResults.proceedWithoutIfNonExact -> return SearchChooser.Choice.ProceedWithout
            }

            val (filteredResults, results) = allSearchResults.partition { result ->
                previouslyDiscardedResults.any { it.name.equals(result.name, ignoreCase = true) }
            }

            val chooseSearchResultData = SearchChooser.Data(
                searchedName, taskData.path, taskData.platform, provider.id, results = results, filteredResults = filteredResults
            )
            return chooser.choose(chooseSearchResultData)
        }

        private fun ProviderSearchResult.toHeader(provider: GameProvider) = ProviderHeader(provider.id, apiUrl, updateDate = now)
    }

    override suspend fun download(taskData: ProviderTaskData, headers: List<ProviderHeader>): List<ProviderData> = taskData.task.run {
        totalWork = headers.size
        message1 = "Downloading '${taskData.name}'..."
        headers.map { header ->
            async(CommonPool) {
                providerRepository.enabledProvider(header.id).download(header.apiUrl, taskData.platform)
                    .apply { incProgress() }
            }
        }.map { it.await() }
    }

    private class CancelSearchException : RuntimeException()
}

interface SearchChooser {
    suspend fun choose(data: Data): Choice

    data class Data(
        val name: String,
        val path: File,
        val platform: Platform,
        val providerId: ProviderId,
        val results: List<ProviderSearchResult>,
        val filteredResults: List<ProviderSearchResult>
    )

    sealed class Choice {
        data class ExactMatch(val result: ProviderSearchResult) : Choice()
        data class NotExactMatch(val result: ProviderSearchResult) : Choice()
        data class NewSearch(val newSearch: String) : Choice()
        data class ExcludeProvider(val provider: ProviderId) : Choice()
        object ProceedWithout : Choice()
        object Cancel : Choice()
    }
}