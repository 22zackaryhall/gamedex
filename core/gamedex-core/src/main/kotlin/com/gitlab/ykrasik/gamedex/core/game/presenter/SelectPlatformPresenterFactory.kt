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

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.app.api.game.ViewCanSelectPlatform
import com.gitlab.ykrasik.gamedex.app.api.util.distincting
import com.gitlab.ykrasik.gamedex.app.api.util.mapping
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.PresenterFactory
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryService
import com.gitlab.ykrasik.gamedex.core.game.GameUserConfig
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 06/06/2018
 * Time: 09:45
 */
@Singleton
class SelectPlatformPresenterFactory @Inject constructor(
    libraryService: LibraryService,
    userConfigRepository: UserConfigRepository
) : PresenterFactory<ViewCanSelectPlatform> {
    private val gameUserConfig = userConfigRepository[GameUserConfig::class]
    private val platformsWithLibraries = libraryService.realLibraries.mapping { it.platform }.distincting()

    override fun present(view: ViewCanSelectPlatform) = object : Presenter() {
        init {
            platformsWithLibraries.bindTo(view.availablePlatforms)

            view.currentPlatform = gameUserConfig.platform
            view.currentPlatformChanges.subscribeOnUi(::onCurrentPlatformChanged)
        }

        private fun onCurrentPlatformChanged(currentPlatform: Platform) {
            gameUserConfig.platform = currentPlatform
        }
    }
}