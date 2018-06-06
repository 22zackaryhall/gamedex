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

package com.gitlab.ykrasik.gamedex.core.game.presenter

import com.gitlab.ykrasik.gamedex.app.api.game.ViewCanSearchGames
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.PresenterFactory
import com.gitlab.ykrasik.gamedex.core.game.GameUserConfig
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 06/06/2018
 * Time: 10:24
 */
@Singleton
class SearchGamesPresenterFactory @Inject constructor(
    userConfigRepository: UserConfigRepository
) : PresenterFactory<ViewCanSearchGames> {
    private val gameUserConfig = userConfigRepository[GameUserConfig::class]

    override fun present(view: ViewCanSearchGames) = object : Presenter() {
        init {
            view.searchText = gameUserConfig.currentPlatformSearch
            view.searchTextChanges.subscribeOnUi(::onSearchTextChanged)
        }

        private fun onSearchTextChanged(searchText: String) {
            gameUserConfig.currentPlatformSearch = searchText
        }
    }
}