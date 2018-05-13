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

package com.gitlab.ykrasik.gamedex.core.game.download

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.ProviderHeader
import com.gitlab.ykrasik.gamedex.app.api.util.Task
import com.gitlab.ykrasik.gamedex.app.api.util.task
import com.gitlab.ykrasik.gamedex.core.api.game.GameService
import com.gitlab.ykrasik.gamedex.core.api.provider.GameProviderService
import com.gitlab.ykrasik.gamedex.core.game.GameUserConfig
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import com.google.inject.ImplementedBy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 29/04/2018
 * Time: 15:54
 */
@ImplementedBy(GameDownloadServiceImpl::class)
interface GameDownloadService {
    fun redownloadAllGames(): Task<Unit>

    fun redownloadGame(game: Game): Task<Game>
}

@Singleton
class GameDownloadServiceImpl @Inject constructor(
    private val gameService: GameService,
    private val gameProviderService: GameProviderService,
    userConfigRepository: UserConfigRepository
) : GameDownloadService {
    private val gameUserConfig = userConfigRepository[GameUserConfig::class]

    override fun redownloadAllGames() = redownloadGames(gameService.games)

    override fun redownloadGame(game: Game) = task("Re-Downloading '${game.name}'...") {
        gameProviderService.checkAtLeastOneProviderEnabled()

        doneMessageOrCancelled("Done: Re-Downloaded '${game.name}'.")
        downloadGame(game, game.providerHeaders)
    }

    private fun redownloadGames(games: List<Game>) = task("Re-Downloading ${games.size} Games...") {
        gameProviderService.checkAtLeastOneProviderEnabled()

        val masterTask = this
        message1 = "Re-Downloading ${games.size} Games..."
        doneMessage { success -> "${if (success) "Done" else "Cancelled"}: Re-Downloaded $processed / $totalWork Games." }

        runSubTask {
            // Operate on a copy of the games to avoid concurrent modifications.
            games.sortedBy { it.name }.forEachWithProgress(masterTask) { game ->
                val providersToDownload = game.providerHeaders.filter { header ->
                    header.updateDate.plus(gameUserConfig.stalePeriod).isBeforeNow
                }
                if (providersToDownload.isNotEmpty()) {
                    downloadGame(game, providersToDownload)
                }
            }
        }
    }

    private suspend fun Task<*>.downloadGame(game: Game, requestedProviders: List<ProviderHeader>): Game {
        val providersToDownload = requestedProviders.filter { gameProviderService.isEnabled(it.id) }
        val downloadedProviderData = runMainTask(gameProviderService.download(game.name, game.platform, game.path, providersToDownload))
        // Replace existing data with new data, pass-through any data that wasn't replaced.
        val newProviderData = game.rawGame.providerData.filterNot { d -> providersToDownload.any { it.id == d.header.id } } + downloadedProviderData
        val newRawGame = game.rawGame.copy(providerData = newProviderData)
        return runMainTask(gameService.replace(game, newRawGame))
    }
}