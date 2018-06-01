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

package com.gitlab.ykrasik.gamedex.javafx.game

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.core.api.file.FileSystemService
import com.gitlab.ykrasik.gamedex.core.api.game.GameService
import com.gitlab.ykrasik.gamedex.core.filter.FilterContextImpl
import com.gitlab.ykrasik.gamedex.core.game.GameUserConfig
import com.gitlab.ykrasik.gamedex.core.game.matchesSearchQuery
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import com.gitlab.ykrasik.gamedex.javafx.*
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import kotlinx.coroutines.experimental.runBlocking
import tornadofx.Controller
import java.util.*
import java.util.function.Predicate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 09/10/2016
 * Time: 14:39
 */
// TODO: Move to tornadoFx di() and have the presenter as a dependency.
@Singleton
class GameController @Inject constructor(
    private val gameService: GameService,
    private val fileSystemService: FileSystemService,
    userConfigRepository: UserConfigRepository
) : Controller() {
    private val gameUserConfig = userConfigRepository[GameUserConfig::class]

    private val mainView: MainView by inject()

    val searchQueryProperty = SimpleStringProperty("")

    private val compositeFilterPredicate = run {
        val context = FilterContextImpl(emptyList(), fileSystemService)
        val filterPredicate: Property<Predicate<Game>> = SimpleObjectProperty(Predicate { true })
        gameUserConfig.currentPlatformFilterSubject.subscribe { filter ->
            filterPredicate.value = Predicate { game ->
                runBlocking {
                    filter!!.evaluate(game, context)
                }
            }
        }

        val searchPredicate = searchQueryProperty.toPredicate { query, game: Game -> game.matchesSearchQuery(query!!) }

        filterPredicate and searchPredicate
    }

    private val nameComparator = Comparator<Game> { o1, o2 -> o1.name.compareTo(o2.name, ignoreCase = true) }
    private val criticScoreComparator = compareBy(Game::criticScore)
    private val userScoreComparator = compareBy(Game::userScore)

    private val sortComparator = gameUserConfig.sortSubject.map { sort ->
        val comparator = when (sort.sortBy) {
            GameUserConfig.SortBy.name_ -> nameComparator
            GameUserConfig.SortBy.criticScore -> criticScoreComparator.then(nameComparator)
            GameUserConfig.SortBy.userScore -> userScoreComparator.then(nameComparator)
            GameUserConfig.SortBy.minScore -> compareBy<Game> { it.minScore }.then(criticScoreComparator).then(userScoreComparator).then(nameComparator)
            GameUserConfig.SortBy.avgScore -> compareBy<Game> { it.avgScore }.then(criticScoreComparator).then(userScoreComparator).then(nameComparator)
            GameUserConfig.SortBy.size -> compareBy<Game> { runBlocking { fileSystemService.size(it.path).await() } }.then(nameComparator)        // FIXME: Hangs UI thread!!!
            GameUserConfig.SortBy.releaseDate -> compareBy(Game::releaseDate).then(nameComparator)
            GameUserConfig.SortBy.updateDate -> compareBy(Game::updateDate)
        }
        if (sort.order == GameUserConfig.SortType.asc) {
            comparator
        } else {
            comparator.reversed()
        }
    }.toBindingCached()

    val games: ObservableList<Game> = gameService.games.toObservableList()
    val platformGames = games.sortedFiltered().apply {
        filteredItems.predicateProperty().bind(gameUserConfig.platformSubject.toBindingCached().toPredicate { platform, game: Game ->
            game.platform == platform
        })
    }
    val sortedFilteredGames: ObservableList<Game> = platformGames.sortedFiltered().apply {
        filteredItems.predicateProperty().bind(compositeFilterPredicate)
        sortedItems.comparatorProperty().bind(sortComparator)
    }

    fun clearFilters() {
        gameUserConfig.currentPlatformFilter = Filter.`true`
        searchQueryProperty.value = ""
    }

    fun viewDetails(game: Game) = mainView.showGameDetails(game)

    fun byId(id: Int): Game = gameService[id]
}