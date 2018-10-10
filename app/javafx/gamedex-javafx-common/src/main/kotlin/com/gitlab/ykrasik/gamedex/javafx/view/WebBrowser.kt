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

package com.gitlab.ykrasik.gamedex.javafx.view

import com.gitlab.ykrasik.gamedex.javafx.Theme
import com.gitlab.ykrasik.gamedex.javafx.map
import com.gitlab.ykrasik.gamedex.javafx.screenBounds
import javafx.beans.property.Property
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import tornadofx.*

/**
 * User: ykrasik
 * Date: 09/06/2017
 * Time: 22:29
 */
class WebBrowser : Fragment() {
    private var webView: WebView by singleAssign()

    private val standalone by lazy { StandaloneBrowserFragment() }
    private lateinit var prevParent: Pane

    override val root = borderpane {
        vgrow = Priority.ALWAYS
        center {
            webView = webview()
        }
        top {
            hbox(spacing = 10.0) {
                paddingAll = 5.0

                button(graphic = Theme.Icon.arrowLeft(18.0)) {
                    enableWhen { canNavigate(back = true) }
                    setOnAction { navigate(back = true) }
                }
                button(graphic = Theme.Icon.arrowRight(18.0)) {
                    enableWhen { canNavigate(back = false) }
                    setOnAction { navigate(back = false) }
                }
                spacer()
                button(graphic = Theme.Icon.maximize(18.0)) {
                    setOnAction { toggleStandalone() }
                }
            }
        }
    }

    // TODO: Find a way to clear browsing history on stop.
    // TODO: Don't stop if in standalone mode.
    fun load(url: String?) = webView.engine.load(url)

    private fun toggleStandalone() {
        if (standalone.isDocked) {
            standalone.close()
        } else {
            openStandalone()
        }
    }

    private fun openStandalone() {
        prevParent = root.parent as Pane
        standalone.openWindow(block = true, owner = null)
        prevParent.children += root
    }

    private fun canNavigate(back: Boolean): Property<Boolean> {
        val history = webView.engine.history
        val entries = history.entries
        return history.currentIndexProperty().map { i ->
            val currentIndex = i!!.toInt()
            entries.size > 1 && (if (back) currentIndex > 0 else currentIndex < entries.size - 1)
        }
    }

    private fun navigate(back: Boolean) = webView.engine.history.go(if (back) -1 else 1)

    private inner class StandaloneBrowserFragment : Fragment() {
        override val root = stackpane {
            minWidth = screenBounds.width * 2 / 3
            minHeight = screenBounds.height * 2 / 3
        }

        init {
            titleProperty.bind(webView.engine.locationProperty())
        }

        override fun onDock() {
            modalStage!!.isMaximized = true
            root.children += this@WebBrowser.root
        }
    }
}