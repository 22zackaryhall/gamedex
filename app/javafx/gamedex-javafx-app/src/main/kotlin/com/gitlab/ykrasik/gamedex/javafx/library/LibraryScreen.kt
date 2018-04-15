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

package com.gitlab.ykrasik.gamedex.javafx.library

import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryEvent
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryRepository
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryView
import com.gitlab.ykrasik.gamedex.core.api.util.BroadcastEventChannel
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.screen.GamedexScreen
import javafx.scene.control.ToolBar
import kotlinx.coroutines.experimental.launch
import tornadofx.*

/**
 * User: ykrasik
 * Date: 09/10/2016
 * Time: 22:17
 */
// TODO: This screen needs some work
// TODO: Show total amount of games and total game size.
class LibraryScreen : GamedexScreen("Libraries", Theme.Icon.hdd()), LibraryView {
    // TODO: Have this data as output of the present() method. Not the repo, but all the libraries.
    private val libraryRepository: LibraryRepository by di()

    override val events = BroadcastEventChannel<LibraryEvent>()

    override fun ToolBar.constructToolbar() {
        addButton { setOnAction { addLibrary() } }
        verticalSeparator()
        editButton {
            disableWhen { root.selectionModel.selectedItemProperty().isNull }
            setOnAction { editLibrary() }
        }
        verticalSeparator()
        spacer()
        verticalSeparator()
        deleteButton("Delete") {
            disableWhen { root.selectionModel.selectedItemProperty().isNull }
            setOnAction { deleteLibrary() }
        }
    }

    override val root = tableview(libraryRepository.libraries.toObservableList()) {
        isEditable = false
        columnResizePolicy = SmartResize.POLICY

        readonlyColumn("Name", Library::name) {
            isSortable = false
            contentWidth(padding = 10.0, useAsMin = true)
        }
        readonlyColumn("Platform", Library::platform) {
            isSortable = false
            contentWidth(padding = 10.0, useAsMin = true)
        }
        readonlyColumn("Path", Library::path) {
            isSortable = false
            contentWidth(padding = 100.0, useAsMin = true)
            remainingWidth()
        }

        contextmenu {
            item("Add", graphic = Theme.Icon.plus(20.0)).action { addLibrary() }
            separator()
            item("Edit", graphic = Theme.Icon.edit(20.0)) {
                disableWhen { this@tableview.selectionModel.selectedItemProperty().isNull }
            }.action { editLibrary() }
            separator()
            item("Delete", graphic = Theme.Icon.delete(20.0)) {
                disableWhen { this@tableview.selectionModel.selectedItemProperty().isNull }
            }.action { deleteLibrary() }

        }

        allowDeselection(onClickAgain = false)
    }

    init {
        libraryRepository.libraries.changesObservable.subscribe {
            root.resizeColumnsToFitContent()
        }
    }

    private fun addLibrary() = launch { events.send(LibraryEvent.AddLibrary) }

    private fun editLibrary() = launch { events.send(LibraryEvent.EditLibrary(selectedLibrary)) }

    private fun deleteLibrary() = launch { events.send(LibraryEvent.DeleteLibrary(selectedLibrary)) }

    private val selectedLibrary: Library get() = root.selectedItem!!
}