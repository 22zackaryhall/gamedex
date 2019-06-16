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

package com.gitlab.ykrasik.gamedex.app.javafx.library

import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.LibraryType
import com.gitlab.ykrasik.gamedex.app.api.file.ViewCanOpenFile
import com.gitlab.ykrasik.gamedex.app.api.library.ViewCanAddOrEditLibrary
import com.gitlab.ykrasik.gamedex.app.api.library.ViewCanDeleteLibrary
import com.gitlab.ykrasik.gamedex.app.api.library.ViewWithLibraries
import com.gitlab.ykrasik.gamedex.app.api.util.channel
import com.gitlab.ykrasik.gamedex.javafx.control.enableWhen
import com.gitlab.ykrasik.gamedex.javafx.control.verticalGap
import com.gitlab.ykrasik.gamedex.javafx.perform
import com.gitlab.ykrasik.gamedex.javafx.settableList
import com.gitlab.ykrasik.gamedex.javafx.state
import com.gitlab.ykrasik.gamedex.javafx.theme.*
import com.gitlab.ykrasik.gamedex.javafx.view.PresentableView
import com.gitlab.ykrasik.gamedex.util.IsValid
import javafx.geometry.Pos
import tornadofx.*
import java.io.File

/**
 * User: ykrasik
 * Date: 12/01/2019
 * Time: 21:34
 */
class LibraryMenu : PresentableView("Libraries", Icons.folders),
    ViewWithLibraries,
    ViewCanAddOrEditLibrary,
    ViewCanDeleteLibrary,
    ViewCanOpenFile {

    override val libraries = settableList<Library>()

    override val canAddOrEditLibraries = state(IsValid.valid)
    override val addOrEditLibraryActions = channel<Library?>()

    override val canDeleteLibraries = state(IsValid.valid)
    override val deleteLibraryActions = channel<Library>()

    override val openFileActions = channel<File>()

    init {
        register()
    }

    override val root = vbox(spacing = 5) {
        addButton(isToolbarButton = false) {
            alignment = Pos.CENTER
            useMaxWidth = true
            tooltip("Add a new library")
            enableWhen(canAddOrEditLibraries)
            action(addOrEditLibraryActions) { null }
        }
        verticalGap()
        vbox {
            libraries.perform { libraries ->
                replaceChildren {
                    gridpane {
                        hgap = 5.0
                        vgap = 3.0
                        libraries.forEach { library ->
                            row {
                                children += if (library.type == LibraryType.Excluded) library.type.icon else library.platform.logo
                                text(library.name)
                                toolbarButton(library.path.toString()) {
                                    useMaxWidth = true
                                    action(openFileActions) { library.path }
                                }
                                editButton(isToolbarButton = false) {
                                    enableWhen(canAddOrEditLibraries)
                                    tooltip("Edit library '${library.name}'")
                                    action(addOrEditLibraryActions) { library }
                                }
                                deleteButton(isToolbarButton = false) {
                                    enableWhen(canDeleteLibraries)
                                    tooltip("Delete library '${library.name}'")
                                    action(deleteLibraryActions) { library }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}