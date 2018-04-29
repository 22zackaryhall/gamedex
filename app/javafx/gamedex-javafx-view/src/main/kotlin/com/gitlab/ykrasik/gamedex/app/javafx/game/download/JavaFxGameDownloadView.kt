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

package com.gitlab.ykrasik.gamedex.app.javafx.game.download

import com.gitlab.ykrasik.gamedex.app.api.game.download.GameDownloadPresenter
import com.gitlab.ykrasik.gamedex.app.api.game.download.GameDownloadView
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.screen.PresentableViewCanRunTask
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import org.controlsfx.control.PopOver
import tornadofx.*

/**
 * User: ykrasik
 * Date: 05/06/2017
 * Time: 10:57
 */
class JavaFxGameDownloadView : PresentableViewCanRunTask<GameDownloadView.Event>(), GameDownloadView {
    private val presenter: GameDownloadPresenter by di()

    private val viewModel = PeriodViewModel()
    override var stalePeriodText by viewModel.stalePeriodTextProperty

    private val stalePeriodValidationErrorProperty = SimpleStringProperty(null)
    override var stalePeriodValidationError by stalePeriodValidationErrorProperty

    init {
        presenter.present(this)

        stalePeriodValidationErrorProperty.onChange { viewModel.validate() }
    }

    override val root = buttonWithPopover("Re-Download", graphic = Theme.Icon.download(), arrowLocation = PopOver.ArrowLocation.TOP_RIGHT, closeOnClick = false) { popover ->
        labeled("Stale Duration", listOf(CommonStyle.headerLabel)) {
            textfield(viewModel.stalePeriodTextProperty) {
                isFocusTraversable = false
                validator(ValidationTrigger.None) {
                    stalePeriodValidationError?.let { error(it) }
                }
            }
        }.apply {
            paddingAll = 5.0
        }
        separator()

        refreshButton("All Stale Games") {
            useMaxWidth = true
            alignment = Pos.CENTER_LEFT
            disableWhen(viewModel.valid.not())
            tooltip("Re-Download all games that were last downloaded before the stale duration")
            setOnAction {
                popover.hide()
                sendEvent(GameDownloadView.Event.RedownloadAllStaleGamesClicked)
            }
        }
    }.apply {
        enableWhen { canRunTaskProperty }
    }

    private inner class PeriodViewModel : ViewModel() {
        val stalePeriodTextProperty = presentableProperty(GameDownloadView.Event::StalePeriodTextChanged) { SimpleStringProperty("") }
    }
}