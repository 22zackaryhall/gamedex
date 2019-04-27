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
import com.gitlab.ykrasik.gamedex.core.EventBus
import com.gitlab.ykrasik.gamedex.core.filter.FilterService
import com.gitlab.ykrasik.gamedex.core.game.GameService
import com.gitlab.ykrasik.gamedex.core.task.Task
import com.gitlab.ykrasik.gamedex.core.task.task
import com.google.inject.ImplementedBy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 31/12/2018
 * Time: 16:00
 */
@ImplementedBy(ResyncGameServiceImpl::class)
interface ResyncGameService {
    fun resyncGame(game: Game)
    fun resyncGames(filter: Filter): Task<Unit>
}

@Singleton
class ResyncGameServiceImpl @Inject constructor(
    private val gameService: GameService,
    private val gameProviderService: GameProviderService,
    private val filterService: FilterService,
    private val eventBus: EventBus
) : ResyncGameService {
    override fun resyncGame(game: Game) = resyncGames(listOf(game))

    override fun resyncGames(filter: Filter) = task("Detecting games to re-sync...") {
        val games = filterService.filter(gameService.games, filter).sortedBy { it.path }
        if (games.isNotEmpty()) {
            resyncGames(games)
        } else {
            successMessage = { "No games matching re-sync filter detected!" }
        }
    }

    private fun resyncGames(games: List<Game>) {
        gameProviderService.checkAtLeastOneProviderEnabled()

        val paths = games.map { game ->
            LibraryPath(game.library, game.path) to game
        }
        eventBus.send(SyncGamesRequestedEvent(paths, isAllowSmartChooseResults = false))
    }
}