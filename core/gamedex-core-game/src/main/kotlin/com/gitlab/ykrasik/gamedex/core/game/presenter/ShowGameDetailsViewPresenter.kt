/****************************************************************************
 * Copyright (C) 2016-2020 Yevgeny Krasik                                   *
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

import com.gitlab.ykrasik.gamedex.app.api.ViewManager
import com.gitlab.ykrasik.gamedex.app.api.game.GameDetailsView
import com.gitlab.ykrasik.gamedex.app.api.game.ViewCanShowGameDetails
import com.gitlab.ykrasik.gamedex.core.EventBus
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.hideViewRequests
import com.gitlab.ykrasik.gamedex.core.util.flowScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 08/06/2018
 * Time: 09:38
 */
@Singleton
class ShowGameDetailsViewPresenter @Inject constructor(
    private val viewManager: ViewManager,
    eventBus: EventBus
) : Presenter<ViewCanShowGameDetails> {
    init {
        flowScope(Dispatchers.Main.immediate) {
            eventBus.hideViewRequests<GameDetailsView>().forEach(debugName = "hideGameDetailsView") { viewManager.hide(it) }
        }
    }

    override fun present(view: ViewCanShowGameDetails) = object : ViewSession() {
        init {
            view.viewGameDetailsActions.forEach(debugName = "showGameDetailsView") { params ->
                viewManager.showGameDetailsView(params)
            }
        }
    }
}