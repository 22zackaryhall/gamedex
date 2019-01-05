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

package com.gitlab.ykrasik.gamedex.app.javafx.game.delete

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.game.DeleteGameView
import com.gitlab.ykrasik.gamedex.javafx.Icons
import com.gitlab.ykrasik.gamedex.javafx.control.jfxCheckBox
import com.gitlab.ykrasik.gamedex.javafx.userMutableState
import com.gitlab.ykrasik.gamedex.javafx.view.ConfirmationWindow
import javafx.beans.property.SimpleObjectProperty
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.stringBinding

/**
 * User: ykrasik
 * Date: 02/05/2018
 * Time: 22:08
 */
class JavaFxDeleteGameView : ConfirmationWindow(icon = Icons.delete), DeleteGameView {
    private val gameProperty = SimpleObjectProperty(Game.Null)
    override var game by gameProperty

    override val fromFileSystem = userMutableState(false)

    init {
        titleProperty.bind(gameProperty.stringBinding { "Delete '${it!!.name}'?" })
        register()
    }

    override val root = buildAreYouSure {
        jfxCheckBox(fromFileSystem.property, "From File System")
    }
}