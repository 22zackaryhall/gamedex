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

package com.gitlab.ykrasik.gamedex.core.game.presenter

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.game.SortBy
import com.gitlab.ykrasik.gamedex.app.api.game.SortOrder
import com.gitlab.ykrasik.gamedex.app.api.game.ViewWithGames
import com.gitlab.ykrasik.gamedex.core.CommonData
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.filter.FilterContextFactory
import com.gitlab.ykrasik.gamedex.core.game.GameSearchService
import com.gitlab.ykrasik.gamedex.core.settings.SettingsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 06/06/2018
 * Time: 09:53
 */
@Singleton
class GamesPresenter @Inject constructor(
    private val commonData: CommonData,
    private val filterContextFactory: FilterContextFactory,
    private val gameSearchService: GameSearchService,
    private val settingsService: SettingsService
) : Presenter<ViewWithGames> {
    private val nameComparator = Comparator<Game> { o1, o2 -> o1.name.compareTo(o2.name, ignoreCase = true) }
    private val criticScoreComparator = compareBy(Game::criticScore)
    private val userScoreComparator = compareBy(Game::userScore)

    override fun present(view: ViewWithGames) = object : ViewSession() {
        init {
            commonData.platformGames.bind(view.games)

            settingsService.game.sortByChannel.combineLatest(settingsService.game.sortOrderChannel).forEach { (sortBy, sortOrder) ->
                val comparator = when (sortBy) {
                    SortBy.Name -> nameComparator
                    SortBy.CriticScore -> criticScoreComparator.then(nameComparator)
                    SortBy.UserScore -> userScoreComparator.then(nameComparator)
                    SortBy.MinScore -> compareBy<Game> { it.minScore }.then(criticScoreComparator).then(userScoreComparator).then(nameComparator)
                    SortBy.MaxScore -> compareBy<Game> { it.maxScore }.then(criticScoreComparator).then(userScoreComparator).then(nameComparator)
                    SortBy.AvgScore -> compareBy<Game> { it.avgScore }.then(criticScoreComparator).then(userScoreComparator).then(nameComparator)
                    SortBy.Size -> compareBy<Game> { it.fileTree.value?.size }.then(nameComparator)
                    SortBy.ReleaseDate -> compareBy(Game::releaseDate).then(nameComparator)
                    SortBy.CreateDate -> compareBy(Game::createDate)
                    SortBy.UpdateDate -> compareBy(Game::updateDate)
                }

                view.sort *= if (sortOrder == SortOrder.Asc) {
                    comparator
                } else {
                    comparator.reversed()
                }
            }

            var platformFilterJob: Job? = null
            settingsService.currentPlatformSettingsChannel.forEach { settings ->
                platformFilterJob?.cancelAndJoin()
                platformFilterJob = settings.filterChannel.combineLatest(settings.searchChannel).forEach { (filter, search) ->
                    val context = filterContextFactory.create(emptyList())
                    val matches = gameSearchService.search(search, settings.platform).mapTo(HashSet()) { it.id }
                    view.filter *= { game: Game ->
                        (search.isBlank() || matches.contains(game.id)) && filter.evaluate(game, context)
                    }
                }
            }
        }
    }
}