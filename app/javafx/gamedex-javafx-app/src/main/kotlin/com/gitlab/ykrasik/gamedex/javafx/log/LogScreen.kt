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

package com.gitlab.ykrasik.gamedex.javafx.log

import ch.qos.logback.classic.Level
import com.gitlab.ykrasik.gamedex.core.general.GeneralUserConfig
import com.gitlab.ykrasik.gamedex.core.log.GamedexLog
import com.gitlab.ykrasik.gamedex.core.log.LogEntry
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.screen.GamedexScreen
import javafx.scene.control.ListCell
import javafx.scene.control.ToolBar
import javafx.scene.input.KeyCombination
import javafx.scene.paint.Color
import tornadofx.*

/**
 * User: ykrasik
 * Date: 28/04/2017
 * Time: 11:14
 */
class LogScreen : GamedexScreen("Log", Theme.Icon.book()) {
    private val userConfigRepository: UserConfigRepository by di()
    private val generalUserConfig = userConfigRepository[GeneralUserConfig::class]

    private val logItems = GamedexLog.entries.toObservableList().sortedFiltered()
    private val logFilterLevelProperty = generalUserConfig.logFilterLevelSubject.toPropertyCached()
    private var displayLevel = logFilterLevelProperty.map(Level::toLevel)

    override fun ToolBar.constructToolbar() {
        header("Level").labelFor =
            popoverComboMenu(
                possibleItems = listOf(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR).map { it.levelStr.toLowerCase().capitalize() },
                selectedItemProperty = logFilterLevelProperty
            ).apply {
                minWidth = 60.0
            }
        header("Tail").labelFor =
            jfxToggleButton(generalUserConfig.logTailSubject.toPropertyCached())
    }

    override val root = listview(logItems) {
        addClass(Style.logView)

        setCellFactory {
            object : ListCell<LogEntry>() {
                init {
                    contextmenu {
                        item("Copy to Clipboard", KeyCombination.keyCombination("ctrl+c")).action {
                            clipboard.putString(item.message)
                        }
                    }
                }

                override fun updateItem(item: LogEntry?, empty: Boolean) {
                    super.updateItem(item, empty)

                    toggleClass(Style.trace, false)
                    toggleClass(Style.debug, false)
                    toggleClass(Style.info, false)
                    toggleClass(Style.warn, false)
                    toggleClass(Style.error, false)

                    if (item == null || empty) {
                        text = null
                        return
                    }

                    text = "${item.timestamp.toString("HH:mm:ss.SSS")} [${item.loggerName}] ${item.message}"

                    when (item.level) {
                        Level.TRACE -> toggleClass(Style.trace, true)
                        Level.DEBUG -> toggleClass(Style.debug, true)
                        Level.INFO -> toggleClass(Style.info, true)
                        Level.WARN -> toggleClass(Style.warn, true)
                        Level.ERROR -> toggleClass(Style.error, true)
                    }
                }
            }
        }

        logItems.onChange {
            if (generalUserConfig.logTail) {
                scrollTo(items.size)
            }
        }
    }

    init {
        logItems.predicate = { entry -> entry.level.isGreaterOrEqual(displayLevel.value) }
        logFilterLevelProperty.onChange { logItems.refilter() }
    }

    class Style : Stylesheet() {
        companion object {
            val logView by cssclass()

            val trace by csspseudoclass()
            val debug by csspseudoclass()
            val info by csspseudoclass()
            val warn by csspseudoclass()
            val error by csspseudoclass()

            init {
                importStylesheet(Style::class)
            }
        }

        init {
            logView {
                listCell {
                    backgroundColor = multi(Color.WHITE) // removes alternating list gray cells.

                    and(trace) {
                        textFill = Color.LIGHTGRAY
                    }
                    and(debug) {
                        textFill = Color.GRAY
                    }
                    and(warn) {
                        textFill = Color.ORANGE
                    }
                    and(error) {
                        textFill = Color.RED
                    }
                    and(selected) {
                        backgroundColor = multi(Color.LIGHTBLUE)
                    }
                }
            }
        }
    }
}