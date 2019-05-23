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

package com.gitlab.ykrasik.gamedex.app.javafx.settings

import com.gitlab.ykrasik.gamedex.app.api.settings.GeneralSettingsView
import com.gitlab.ykrasik.gamedex.javafx.control.horizontalField
import com.gitlab.ykrasik.gamedex.javafx.control.jfxCheckBox
import com.gitlab.ykrasik.gamedex.javafx.theme.Icons
import com.gitlab.ykrasik.gamedex.javafx.userMutableState
import com.gitlab.ykrasik.gamedex.javafx.view.PresentableTabView
import tornadofx.fieldset
import tornadofx.form

/**
 * User: ykrasik
 * Date: 15/05/2019
 * Time: 22:22
 */
class JavaFxGeneralSettingsView : PresentableTabView("General", Icons.tune), GeneralSettingsView {
    override val useInternalBrowser = userMutableState(true)

    override val root = form {
        fieldset {
            horizontalField("Use Internal Browser") { jfxCheckBox(useInternalBrowser.property) }
        }
    }

    init {
        register()
    }
}