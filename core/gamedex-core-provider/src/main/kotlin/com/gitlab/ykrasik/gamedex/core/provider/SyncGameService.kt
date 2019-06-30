/****************************************************************************
 * Copyright (C) 2016-2019 Yevgeny Krasik                                   *
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

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.LibraryPath
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.app.api.util.MultiChannel
import com.gitlab.ykrasik.gamedex.core.CoreEvent
import com.gitlab.ykrasik.gamedex.core.EventBus
import com.gitlab.ykrasik.gamedex.core.filter.FilterService
import com.gitlab.ykrasik.gamedex.core.game.GameService
import com.gitlab.ykrasik.gamedex.core.on
import com.gitlab.ykrasik.gamedex.core.task.task
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import com.gitlab.ykrasik.gamedex.provider.id
import com.gitlab.ykrasik.gamedex.provider.supports
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 31/12/2018
 * Time: 16:00
 */
@Singleton
class SyncGameServiceImpl @Inject constructor(
    private val gameService: GameService,
    private val gameProviderService: GameProviderService,
    private val filterService: FilterService,
    private val eventBus: EventBus
) : SyncGameService {
    override val isGameSyncRunning = MultiChannel.conflated(false).apply {
        eventBus.on<SyncGamesEvent.Started> { send(true) }
        eventBus.on<SyncGamesEvent.Finished> { send(false) }
    }

    override fun syncGame(game: Game) = syncGames(listOf(toRequest(game, emptyList())), isAllowSmartChooseResults = false)

    override fun detectGamesWithMissingProviders(filter: Filter, syncOnlyMissingProviders: Boolean) = task("Detecting games with missing providers...") {
        val games = filterService.filter(gameService.games, filter).asSequence()
            .map { it to it.getMissingProviders() }
            .filter { (_, missingProviders) -> missingProviders.isNotEmpty() }
            .map { (game, missingProviders) -> game to missingProviders.takeIf { syncOnlyMissingProviders }.orEmpty() }
            .toList()
            .sortedBy { (game, _) -> game.path }

        successMessage = { "${games.size} games." }
        games.map { (game, missingProviders) -> toRequest(game, missingProviders) }
    }

    private fun toRequest(game: Game, providersToSync: List<ProviderId>) = SyncPathRequest(
        libraryPath = LibraryPath(game.library, game.path),
        existingGame = game,
        syncOnlyTheseProviders = providersToSync
    )

    override fun syncGames(requests: List<SyncPathRequest>, isAllowSmartChooseResults: Boolean) {
        gameProviderService.assertHasEnabledProvider()

        if (requests.isNotEmpty()) {
            eventBus.send(SyncGamesEvent.RequestSync(requests, isAllowSmartChooseResults))
        }
    }

    private fun Game.getMissingProviders(): List<ProviderId> {
        val existingProviders = existingProviders.toList() + excludedProviders
        return gameProviderService.enabledProviders.asSequence()
            .filterNot { provider -> provider.supports(platform) && provider.id in existingProviders }
            .map { it.id }
            .toList()
    }
}

sealed class SyncGamesEvent : CoreEvent {
    data class RequestSync(val requests: List<SyncPathRequest>, val isAllowSmartChooseResults: Boolean) : SyncGamesEvent()
    object Started : SyncGamesEvent()
    object Finished : SyncGamesEvent()
}