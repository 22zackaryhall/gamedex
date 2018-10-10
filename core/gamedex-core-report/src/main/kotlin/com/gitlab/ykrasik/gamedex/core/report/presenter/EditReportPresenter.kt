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

package com.gitlab.ykrasik.gamedex.core.report.presenter

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.ViewManager
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.app.api.report.EditReportView
import com.gitlab.ykrasik.gamedex.app.api.report.ReportData
import com.gitlab.ykrasik.gamedex.app.api.task.TaskRunner
import com.gitlab.ykrasik.gamedex.core.Presentation
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.game.GameService
import com.gitlab.ykrasik.gamedex.core.report.ReportService
import com.gitlab.ykrasik.gamedex.util.setAll
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 24/06/2018
 * Time: 10:33
 */
@Singleton
class EditReportPresenter @Inject constructor(
    private val reportService: ReportService,
    private val gameService: GameService,
    private val taskRunner: TaskRunner,
    private val viewManager: ViewManager
) : Presenter<EditReportView> {
    override fun present(view: EditReportView) = object : Presentation() {
        init {
            view.nameChanges.forEach { onNameChanged() }
            view.filterChanges.forEach { onFilterChanged() }

            view.unexcludeGameActions.forEach { onUnexcludeGame(it) }
            view.acceptActions.forEach { onAccept() }
            view.cancelActions.forEach { onCancel() }
        }

        override fun onShow() {
            val report = view.report
            view.name = report?.name ?: ""
            view.filter = report?.filter ?: Filter.`true`
            view.excludedGames.setAll(report?.excludedGames?.map { gameService[it] } ?: emptyList())
            validateName()
        }

        private fun onNameChanged() {
            validateName()
        }

        private fun onFilterChanged() {
        }

        private fun validateName() {
            view.nameValidationError = when {
                view.name.isEmpty() -> "Name is required!"
                nameAlreadyUsed -> "Name already in use!"
                else -> null
            }
        }

        private val nameAlreadyUsed get() = view.name in (reportService.reports.map { it.name } - view.report?.name)

        private fun onUnexcludeGame(game: Game) {
            view.excludedGames -= game
        }

        private suspend fun onAccept() {
            check(view.nameValidationError == null) { "Cannot accept invalid state!" }
            val newReportData = ReportData(
                name = view.name,
                filter = view.filter,
                excludedGames = view.excludedGames.map { it.id }
            )
            taskRunner.runTask(
                if (view.report != null) {
                    reportService.update(view.report!!, newReportData)
                } else {
                    reportService.add(newReportData)
                }
            )
            close()
        }

        private fun onCancel() {
            close()
        }

        private fun close() {
            viewManager.closeEditReportView(view)
        }
    }
}