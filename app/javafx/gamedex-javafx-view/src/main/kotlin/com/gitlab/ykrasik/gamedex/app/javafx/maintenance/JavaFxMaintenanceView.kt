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

package com.gitlab.ykrasik.gamedex.app.javafx.maintenance

import com.gitlab.ykrasik.gamedex.app.api.game.ViewCanRedownloadGames
import com.gitlab.ykrasik.gamedex.app.api.maintenance.ClearUserDataView
import com.gitlab.ykrasik.gamedex.app.api.maintenance.ExportDatabaseView
import com.gitlab.ykrasik.gamedex.app.api.maintenance.ImportDatabaseView
import com.gitlab.ykrasik.gamedex.app.api.maintenance.ViewCanCleanupDatabase
import com.gitlab.ykrasik.gamedex.app.api.util.channel
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.control.verticalGap
import com.gitlab.ykrasik.gamedex.javafx.view.PresentableTabView
import com.gitlab.ykrasik.gamedex.util.browse
import javafx.geometry.Pos
import tornadofx.*
import java.io.File

/**
 * User: ykrasik
 * Date: 05/06/2017
 * Time: 14:57
 */
class JavaFxMaintenanceView : PresentableTabView("Maintain", Icons.wrench),
    ExportDatabaseView, ImportDatabaseView, ClearUserDataView, ViewCanCleanupDatabase, ViewCanRedownloadGames {

    override val exportDatabaseActions = channel<Unit>()
    override val importDatabaseActions = channel<Unit>()
    override val clearUserDataActions = channel<Unit>()
    override val cleanupDatabaseActions = channel<Unit>()
    override val redownloadGamesActions = channel<Unit>()

    init {
        viewRegistry.onCreate(this)
    }

    override val root = vbox(spacing = 5) {
        confirmButton("Export Database", Icons.export) {
            useMaxWidth = true
            alignment = Pos.CENTER_LEFT
            eventOnAction(exportDatabaseActions)
        }
        warningButton("Import Database", Icons.import) {
            useMaxWidth = true
            alignment = Pos.CENTER_LEFT
            eventOnAction(importDatabaseActions)
        }

        verticalGap()

        infoButton("Re-Download Games", Icons.download) {
            useMaxWidth = true
            alignment = Pos.CENTER_LEFT
            eventOnAction(redownloadGamesActions)
        }

        verticalGap()

        deleteButton("Clear User Data") {
            useMaxWidth = true
            graphic = Icons.databaseCleanup
            alignment = Pos.CENTER_LEFT
            tooltip("Clear game user data, like tags, excluded providers or custom thumbnails for all games.")
            eventOnAction(clearUserDataActions)
        }
        deleteButton("Cleanup Database") {
            useMaxWidth = true
            graphic = Icons.databaseCleanup
            alignment = Pos.CENTER_LEFT
            tooltip("Cleanup stale data, like games linked to paths that no longer exist, unused images & file structure cache for deleted games.")
            eventOnAction(cleanupDatabaseActions)
        }
    }

    override fun selectDatabaseExportDirectory(initialDirectory: File?) =
        chooseDirectory("Select Database Export Folder...", initialDirectory)

    override fun selectDatabaseImportFile(initialDirectory: File?) =
        chooseFile("Select Database File...", filters = emptyArray()) {
            this@chooseFile.initialDirectory = initialDirectory
        }.firstOrNull()

    override fun browseDirectory(directory: File) = browse(directory)

    override fun confirmImportDatabase() = areYouSureDialog("This will overwrite the existing database.")

    override fun confirmClearUserData() = areYouSureDialog("Clear game user data?") {
        text("This will remove tags, excluded providers & any custom information entered (like custom names or thumbnails) from all games.") {
            wrappingWidth = 400.0
        }
    }
}