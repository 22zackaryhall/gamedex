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

package com.gitlab.ykrasik.gamedex.core.provider.presenter

import com.gitlab.ykrasik.gamedex.app.api.provider.ResyncGamesView
import com.gitlab.ykrasik.gamedex.core.EventBus
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.provider.ResyncGameService
import com.gitlab.ykrasik.gamedex.core.settings.SettingsService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 06/05/2018
 * Time: 09:51
 */
@Singleton
class ResyncGamesPresenter @Inject constructor(
    private val settingsService: SettingsService,
    private val resyncGameService: ResyncGameService,
    private val eventBus: EventBus
) : Presenter<ResyncGamesView> {
    override fun present(view: ResyncGamesView) = object : ViewSession() {
        init {
//            view.redownloadGamesCondition.forEach { setCanAccept() }
            view.resyncGamesConditionIsValid.forEach { setCanAccept() }
            view.acceptActions.forEach { onAccept() }
            view.cancelActions.forEach { onCancel() }
        }

        override fun onShow() {
            view.resyncGamesCondition *= settingsService.providerGeneral.resyncGamesCondition
            setCanAccept()
        }

        private fun setCanAccept() {
            view.canAccept *= view.resyncGamesConditionIsValid.value
        }

        private suspend fun onAccept() {
            view.canAccept.assert()

            val condition = view.resyncGamesCondition.value
            settingsService.providerGeneral.modify { copy(resyncGamesCondition = condition) }

            resyncGameService.resyncGames(condition)

            finished()
        }

        private fun onCancel() {
            finished()
        }

        private fun finished() = eventBus.viewFinished(view)
    }
}