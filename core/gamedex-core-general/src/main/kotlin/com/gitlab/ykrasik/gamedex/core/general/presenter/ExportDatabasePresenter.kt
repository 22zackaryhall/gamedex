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

package com.gitlab.ykrasik.gamedex.core.general.presenter

import com.gitlab.ykrasik.gamedex.app.api.general.ExportDatabaseView
import com.gitlab.ykrasik.gamedex.core.Presentation
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.general.DatabaseActionsService
import com.gitlab.ykrasik.gamedex.core.settings.SettingsService
import com.gitlab.ykrasik.gamedex.core.task.TaskService
import com.gitlab.ykrasik.gamedex.util.now
import org.joda.time.DateTimeZone
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 06/05/2018
 * Time: 13:21
 */
@Singleton
class ExportDatabasePresenter @Inject constructor(
    private val databaseActionsService: DatabaseActionsService,
    private val taskService: TaskService,
    private val settingsService: SettingsService
) : Presenter<ExportDatabaseView> {

    override fun present(view: ExportDatabaseView) = object : Presentation() {
        init {
            view.exportDatabaseActions.forEach { exportDatabase() }
        }

        private suspend fun exportDatabase() {
            val selectedDirectory = view.selectDatabaseExportDirectory(settingsService.general.exportDbDirectory) ?: return
            settingsService.general.modify { copy(exportDbDirectory = selectedDirectory) }
            val timestamp = now.withZone(DateTimeZone.getDefault())
            val timestamptedPath = Paths.get(
                selectedDirectory.toString(),
                timestamp.toString("yyyy-MM-dd"),
                "db_${timestamp.toString("HH_mm_ss")}.json"
            ).toFile()

            taskService.execute(databaseActionsService.exportDatabase(timestamptedPath))
            view.browseDirectory(timestamptedPath.parentFile)
        }
    }
}