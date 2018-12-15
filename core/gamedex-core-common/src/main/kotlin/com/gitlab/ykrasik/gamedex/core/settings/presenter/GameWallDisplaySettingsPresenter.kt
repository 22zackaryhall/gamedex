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

package com.gitlab.ykrasik.gamedex.core.settings.presenter

import com.gitlab.ykrasik.gamedex.app.api.settings.ViewCanChangeGameWallDisplaySettings
import com.gitlab.ykrasik.gamedex.app.api.settings.ViewWithGameWallDisplaySettings
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.settings.SettingsService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 10/06/2018
 * Time: 12:30
 */
@Singleton
class ChangeGameWallDisplaySettingsPresenter @Inject constructor(
    private val settingsService: SettingsService
) : Presenter<ViewCanChangeGameWallDisplaySettings> {
    override fun present(view: ViewCanChangeGameWallDisplaySettings) = object : ViewSession() {
        init {
            with(view.mutableGameWallDisplaySettings) {
                settingsService.cellDisplay.bind({ imageDisplayTypeChannel }, ::imageDisplayType, imageDisplayTypeChanges) { copy(imageDisplayType = it) }
                settingsService.cellDisplay.bind({ showBorderChannel }, ::showBorder, showBorderChanges) { copy(showBorder = it) }
                settingsService.cellDisplay.bind({ widthChannel }, ::width, widthChanges) { copy(width = it) }
                settingsService.cellDisplay.bind({ heightChannel }, ::height, heightChanges) { copy(height = it) }
                settingsService.cellDisplay.bind({ horizontalSpacingChannel }, ::horizontalSpacing, horizontalSpacingChanges) { copy(horizontalSpacing = it) }
                settingsService.cellDisplay.bind({ verticalSpacingChannel }, ::verticalSpacing, verticalSpacingChanges) { copy(verticalSpacing = it) }
            }
        }
    }
}

@Singleton
class GameWallDisplaySettingsPresenter @Inject constructor(
    private val settingsService: SettingsService
) : Presenter<ViewWithGameWallDisplaySettings> {
    override fun present(view: ViewWithGameWallDisplaySettings) = object : ViewSession() {
        init {
            val settings = settingsService.cellDisplay
            with(view.gameWallDisplaySettings) {
                settings.imageDisplayTypeChannel.reportChangesTo(::imageDisplayType)
                settings.showBorderChannel.reportChangesTo(::showBorder)
                settings.widthChannel.reportChangesTo(::width)
                settings.heightChannel.reportChangesTo(::height)
                settings.horizontalSpacingChannel.reportChangesTo(::horizontalSpacing)
                settings.verticalSpacingChannel.reportChangesTo(::verticalSpacing)
            }
        }
    }
}