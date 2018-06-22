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

package com.gitlab.ykrasik.gamedex.javafx.settings

import com.gitlab.ykrasik.gamedex.core.settings.SettingsService
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import com.gitlab.ykrasik.gamedex.util.logger
import tornadofx.Controller
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 09/05/2017
 * Time: 17:09
 */
// TODO: Move to tornadoFx di() and have the presenter as a dependency.
@Singleton
class SettingsController @Inject constructor(private val userConfigRepository: UserConfigRepository,
                                             private val settingsService: SettingsService) : Controller() {
    private val logger = logger()

    private val settingsView: SettingsView by inject()

    suspend fun showSettingsMenu() {
        userConfigRepository.saveSnapshot()
        settingsService.saveSnapshot()
        try {
            val accept = settingsView.show()
            if (accept) {
                userConfigRepository.commitSnapshot()
                settingsService.commitSnapshot()
            } else {
                userConfigRepository.revertSnapshot()
                settingsService.revertSnapshot()
            }
        } catch (e: Exception) {
            logger.error("Error updating settings!", e)
            userConfigRepository.revertSnapshot()
            settingsService.revertSnapshot()
        }
    }
}