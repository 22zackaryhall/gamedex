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

package com.gitlab.ykrasik.gamedex.core.game.details

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.game.details.GameDetailsPresenter
import com.gitlab.ykrasik.gamedex.app.api.game.details.GameDetailsPresenterFactory
import com.gitlab.ykrasik.gamedex.app.api.game.details.GameDetailsView
import com.gitlab.ykrasik.gamedex.core.api.image.ImageRepository
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 29/04/2018
 * Time: 20:22
 */
@Singleton
class GameDetailsPresenterFactoryImpl @Inject constructor(
    private val imageRepository: ImageRepository
) : GameDetailsPresenterFactory {
    override fun present(view: GameDetailsView): GameDetailsPresenter = object : GameDetailsPresenter {
        override fun onShow(game: Game) {
            view.game = game
            view.displayWebPage(youTubeSearchUrl(game))
            view.poster = if (game.posterUrl != null) {
                imageRepository.fetchImage(game.posterUrl!!, game.id, persistIfAbsent = false)
            } else {
                null
            }
        }

        private fun youTubeSearchUrl(game: Game) =
            "https://www.youtube.com/results?search_query=${URLEncoder.encode("${game.name} ${game.platform} gameplay", "utf-8")}"

    }
}