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

import com.gitlab.ykrasik.gamedex.app.api.game.ViewWithGameSort
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.settings.SettingsService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 10/06/2018
 * Time: 17:51
 */
@Singleton
class GameSortPresenter @Inject constructor(
    private val settingsService: SettingsService
) : Presenter<ViewWithGameSort> {
    override fun present(view: ViewWithGameSort) = object : ViewSession() {
        init {
            settingsService.game.bind({ sortByChannel }, view.sortBy) { copy(sortBy = it) }
            settingsService.game.bind({ sortOrderChannel }, view.sortOrder) { copy(sortOrder = it) }
        }
    }
}