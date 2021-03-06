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

package com.gitlab.ykrasik.gamedex.app.javafx.maintenance

import com.gitlab.ykrasik.gamedex.app.api.maintenance.CleanupDatabaseView
import com.gitlab.ykrasik.gamedex.app.api.maintenance.StaleData
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.control.*
import com.gitlab.ykrasik.gamedex.javafx.theme.GameDexStyle
import com.gitlab.ykrasik.gamedex.javafx.theme.Icons
import com.gitlab.ykrasik.gamedex.javafx.theme.color
import com.gitlab.ykrasik.gamedex.javafx.view.ConfirmationWindow
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

/**
 * User: ykrasik
 * Date: 16/12/2018
 * Time: 09:57
 */
class JavaFxCleanupDatabaseView : ConfirmationWindow("Cleanup Database", Icons.databaseCleanup), CleanupDatabaseView {
    override val staleData = viewMutableStateFlow(StaleData.Null, debugName = "staleData")

    override val isDeleteLibrariesAndGames = viewMutableStateFlow(false, debugName = "isDeleteLibrariesAndGames")
    override val isDeleteImages = viewMutableStateFlow(false, debugName = "isDeleteImages")
    override val isDeleteFileCache = viewMutableStateFlow(false, debugName = "isDeleteFileCache")

    init {
        register()
    }

    override val root = borderpane {
        prefWidth = 600.0
        minHeight = 300.0
        top = confirmationToolbar()
        center = form {
            paddingAll = 10
            fieldset("Select stale data to delete") {
                horizontalField("Libraries & Games") {
                    label.graphic = Icons.hdd.color(Color.BLACK)
                    showWhen { staleData.property.map { it.libraries.isNotEmpty() || it.games.isNotEmpty() } }
                    jfxCheckBox(isDeleteLibrariesAndGames.property)

                    viewButton(staleData.property.typesafeStringBinding { "${it.libraries.size} Libraries" }) {
                        prettyListView(staleData.property.mapToList { it.libraries.map { it.path } })
                    }.apply {
                        showWhen { staleData.property.typesafeBooleanBinding { it.libraries.isNotEmpty() } }
                    }

                    viewButton(staleData.property.typesafeStringBinding { "${it.games.size} Games" }) {
                        prettyListView(staleData.property.mapToList { it.games.map { it.path } })
                    }.apply {
                        showWhen { staleData.property.typesafeBooleanBinding { it.games.isNotEmpty() } }
                    }
                }
                horizontalField("Images") {
                    label.graphic = Icons.thumbnail
                    showWhen { staleData.property.map { it.images.isNotEmpty() } }
                    jfxCheckBox(isDeleteImages.property)

                    viewButton(staleData.property.typesafeStringBinding { "${it.images.size} Images: ${it.staleImagesSizeTaken.humanReadable}" }) {
                        prettyListView(staleData.property.mapToList { it.images.map { "${it.key} [${it.value}]" } })
                    }.apply {
                        showWhen { staleData.property.typesafeBooleanBinding { it.images.isNotEmpty() } }
                    }
                }
                horizontalField("File Cache") {
                    label.graphic = Icons.fileQuestion
                    showWhen { staleData.property.map { it.fileTrees.isNotEmpty() } }
                    jfxCheckBox(isDeleteFileCache.property)

                    viewButton(staleData.property.typesafeStringBinding { "${it.fileTrees.size} File Cache Entries: ${it.staleFileTreesSizeTaken.humanReadable}" }) {
                        prettyListView(staleData.property.mapToList { it.fileTrees.map { "${it.key} [${it.value}]" } })
                    }.apply {
                        showWhen { staleData.property.typesafeBooleanBinding { it.fileTrees.isNotEmpty() } }
                    }
                }
            }
        }
    }

    private inline fun EventTarget.viewButton(textProperty: ObservableValue<String>, crossinline op: VBox.() -> Unit = {}) =
        buttonWithPopover("", Icons.details) {
            (popOver.contentNode as ScrollPane).minWidth = 600.0
            op()
        }.apply {
            addClass(GameDexStyle.infoButton)
            textProperty().bind(textProperty)
        }
}