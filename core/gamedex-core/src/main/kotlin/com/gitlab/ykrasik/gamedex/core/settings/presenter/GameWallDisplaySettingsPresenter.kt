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

package com.gitlab.ykrasik.gamedex.core.settings.presenter

import com.gitlab.ykrasik.gamedex.app.api.settings.ViewCanChangeGameWallDisplaySettings
import com.gitlab.ykrasik.gamedex.app.api.settings.ViewWithGameWallDisplaySettings
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.settings.GameCellDisplaySettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 10/06/2018
 * Time: 12:30
 */
@Singleton
class ChangeGameWallDisplaySettingsPresenter @Inject constructor(
    private val settingsRepo: GameCellDisplaySettingsRepository
) : Presenter<ViewCanChangeGameWallDisplaySettings> {
    override fun present(view: ViewCanChangeGameWallDisplaySettings) = object : ViewSession() {
        init {
            with(view.mutableGameWallDisplaySettings) {
                imageDisplayType.bindBidirectional(settingsRepo.imageDisplayType)
                showBorder.bindBidirectional(settingsRepo.showBorder)
                width.bindBidirectional(settingsRepo.width)
                height.bindBidirectional(settingsRepo.height)
                horizontalSpacing.bindBidirectional(settingsRepo.horizontalSpacing)
                verticalSpacing.bindBidirectional(settingsRepo.verticalSpacing)
            }
        }
    }
}

@Singleton
class GameWallDisplaySettingsPresenter @Inject constructor(
    private val settingsRepo: GameCellDisplaySettingsRepository
) : Presenter<ViewWithGameWallDisplaySettings> {
    override fun present(view: ViewWithGameWallDisplaySettings) = object : ViewSession() {
        init {
            with(view.gameWallDisplaySettings) {
                imageDisplayType *= settingsRepo.imageDisplayType withDebugName "imageDisplayType"
                showBorder *= settingsRepo.showBorder withDebugName "showBorder"
                width *= settingsRepo.width withDebugName "width"
                height *= settingsRepo.height withDebugName "height"
                horizontalSpacing *= settingsRepo.horizontalSpacing withDebugName "horizontalSpacing"
                verticalSpacing *= settingsRepo.verticalSpacing withDebugName "verticalSpacing"
            }
        }
    }
}