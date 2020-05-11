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

import com.gitlab.ykrasik.gamedex.app.api.settings.*
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.settings.GameOverlayDisplaySettingsRepository
import com.gitlab.ykrasik.gamedex.core.settings.MetaTagDisplaySettingsRepository
import com.gitlab.ykrasik.gamedex.core.settings.NameDisplaySettingsRepository
import com.gitlab.ykrasik.gamedex.core.settings.VersionDisplaySettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 09/06/2018
 * Time: 18:59
 */
abstract class ChangeGameOverlayDisplaySettingsPresenter<V> : Presenter<V> {
    override fun present(view: V) = object : ViewSession() {
        init {
            with(extractOverlay(view)) {
                enabled.bindBidirectional(settingsRepo.enabled)
                showOnlyWhenActive.bindBidirectional(settingsRepo.showOnlyWhenActive)
                position.bindBidirectional(settingsRepo.position)
                fillWidth.bindBidirectional(settingsRepo.fillWidth)
                fontSize.bindBidirectional(settingsRepo.fontSize)
                boldFont.bindBidirectional(settingsRepo.boldFont)
                italicFont.bindBidirectional(settingsRepo.italicFont)
                textColor.bindBidirectional(settingsRepo.textColor)
                backgroundColor.bindBidirectional(settingsRepo.backgroundColor)
                opacity.bindBidirectional(settingsRepo.opacity)
            }
        }
    }

    protected abstract val settingsRepo: GameOverlayDisplaySettingsRepository
    protected abstract fun extractOverlay(view: V): MutableOverlayDisplaySettings
}

@Singleton
class ChangeGameNameOverlayDisplaySettingsPresenter @Inject constructor(
    @NameDisplaySettingsRepository override val settingsRepo: GameOverlayDisplaySettingsRepository
) : ChangeGameOverlayDisplaySettingsPresenter<ViewCanChangeNameOverlayDisplaySettings>() {
    override fun extractOverlay(view: ViewCanChangeNameOverlayDisplaySettings) = view.mutableNameOverlayDisplaySettings
}

@Singleton
class ChangeGameMetaTagOverlayDisplaySettingsPresenter @Inject constructor(
    @MetaTagDisplaySettingsRepository override val settingsRepo: GameOverlayDisplaySettingsRepository
) : ChangeGameOverlayDisplaySettingsPresenter<ViewCanChangeMetaTagOverlayDisplaySettings>() {
    override fun extractOverlay(view: ViewCanChangeMetaTagOverlayDisplaySettings) = view.mutableMetaTagOverlayDisplaySettings
}

@Singleton
class ChangeGameVersionOverlayDisplaySettingsPresenter @Inject constructor(
    @VersionDisplaySettingsRepository override val settingsRepo: GameOverlayDisplaySettingsRepository
) : ChangeGameOverlayDisplaySettingsPresenter<ViewCanChangeVersionOverlayDisplaySettings>() {
    override fun extractOverlay(view: ViewCanChangeVersionOverlayDisplaySettings) = view.mutableVersionOverlayDisplaySettings
}

abstract class GameOverlayDisplaySettingsPresenter<V> : Presenter<V> {
    override fun present(view: V) = object : ViewSession() {
        init {
            with(extractOverlay(view)) {
                enabled *= settingsRepo.enabled withDebugName "enabled"
                showOnlyWhenActive *= settingsRepo.showOnlyWhenActive withDebugName "showOnlyWhenActive"
                position *= settingsRepo.position withDebugName "position"
                fillWidth *= settingsRepo.fillWidth withDebugName "fillWidth"
                fontSize *= settingsRepo.fontSize withDebugName "fontSize"
                boldFont *= settingsRepo.boldFont withDebugName "boldFont"
                italicFont *= settingsRepo.italicFont withDebugName "italicFont"
                textColor *= settingsRepo.textColor withDebugName "textColor"
                backgroundColor *= settingsRepo.backgroundColor withDebugName "backgroundColor"
                opacity *= settingsRepo.opacity withDebugName "opacity"
            }
        }
    }

    protected abstract val settingsRepo: GameOverlayDisplaySettingsRepository
    protected abstract fun extractOverlay(view: V): OverlayDisplaySettings
}

@Singleton
class GameNameOverlayDisplaySettingsPresenter @Inject constructor(
    @NameDisplaySettingsRepository override val settingsRepo: GameOverlayDisplaySettingsRepository
) : GameOverlayDisplaySettingsPresenter<ViewWithNameOverlayDisplaySettings>() {
    override fun extractOverlay(view: ViewWithNameOverlayDisplaySettings) = view.nameOverlayDisplaySettings
}

@Singleton
class GameMetaTagOverlayDisplaySettingsPresenter @Inject constructor(
    @MetaTagDisplaySettingsRepository override val settingsRepo: GameOverlayDisplaySettingsRepository
) : GameOverlayDisplaySettingsPresenter<ViewWithMetaTagOverlayDisplaySettings>() {
    override fun extractOverlay(view: ViewWithMetaTagOverlayDisplaySettings) = view.metaTagOverlayDisplaySettings
}

@Singleton
class GameVersionOverlayDisplaySettingsPresenter @Inject constructor(
    @VersionDisplaySettingsRepository override val settingsRepo: GameOverlayDisplaySettingsRepository
) : GameOverlayDisplaySettingsPresenter<ViewWithVersionOverlayDisplaySettings>() {
    override fun extractOverlay(view: ViewWithVersionOverlayDisplaySettings) = view.versionOverlayDisplaySettings
}