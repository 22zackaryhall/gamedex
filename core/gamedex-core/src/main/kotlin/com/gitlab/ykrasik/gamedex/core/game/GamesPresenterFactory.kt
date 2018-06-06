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

package com.gitlab.ykrasik.gamedex.core.game

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.game.ViewWithGames
import com.gitlab.ykrasik.gamedex.app.api.util.filtering
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.PresenterFactory
import com.gitlab.ykrasik.gamedex.core.api.file.FileSystemService
import com.gitlab.ykrasik.gamedex.core.api.game.GameService
import com.gitlab.ykrasik.gamedex.core.api.util.toConflatedChannel
import com.gitlab.ykrasik.gamedex.core.filter.FilterContextImpl
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import kotlinx.coroutines.experimental.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 06/06/2018
 * Time: 09:53
 */
@Singleton
class GamesPresenterFactory @Inject constructor(
    gameService: GameService,
    fileSystemService: FileSystemService,
    userConfigRepository: UserConfigRepository
) : PresenterFactory<ViewWithGames> {
    private val gameUserConfig = userConfigRepository[GameUserConfig::class]

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
    }

    private val platformPredicate = gameUserConfig.platformSubject.toConflatedChannel { platform ->
        { game: Game -> game.platform == platform }
    }

    // The platform doesn't change that often, so an unoptimized filter is acceptable here.
    private val platformGames = gameService.games.filtering(platformPredicate)

    private val filterPredicate = gameUserConfig.currentPlatformSettingsSubject.map { settings ->
        val context = FilterContextImpl(emptyList(), fileSystemService)
        return@map { game: Game ->
            game.matchesSearchQuery(settings.search) &&
            runBlocking {
                settings.filter.evaluate(game, context)
            }
        }
    }

    override fun present(view: ViewWithGames) = object : Presenter() {
        init {
            platformGames.bindTo(view.games)
            sortComparator.subscribe { view.sort = it }
            filterPredicate.subscribe { view.filter = it }
        }
    }
}