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

package com.gitlab.ykrasik.gamedex.app.javafx.provider

import com.gitlab.ykrasik.gamedex.app.api.provider.ResyncGamesView
import com.gitlab.ykrasik.gamedex.app.javafx.filter.JavaFxFilterView
import com.gitlab.ykrasik.gamedex.javafx.theme.Icons
import com.gitlab.ykrasik.gamedex.javafx.userMutableState
import com.gitlab.ykrasik.gamedex.javafx.view.ConfirmationWindow
import tornadofx.borderpane
import tornadofx.paddingAll
import tornadofx.scrollpane

/**
 * User: ykrasik
 * Date: 31/12/2018
 * Time: 16:25
 */
class JavaFxResyncGamesView : ConfirmationWindow("Re-Sync Games", Icons.sync), ResyncGamesView {
    private val filterView = JavaFxFilterView(onlyShowFiltersForCurrentPlatform = false)

    override val resyncGamesFilter = filterView.externalMutations
    override val resyncGamesFilterIsValid = userMutableState(filterView.filterIsValid)

    init {
        register()
    }

    override val root = borderpane {
        top = confirmationToolbar()
        center = scrollpane {
            paddingAll = 10
            add(filterView.root)
        }
    }
}