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

package com.gitlab.ykrasik.gamedex.javafx.control

import com.gitlab.ykrasik.gamedex.javafx.importStylesheetSafe
import com.gitlab.ykrasik.gamedex.javafx.theme.Colors
import com.gitlab.ykrasik.gamedex.util.logger
import com.gitlab.ykrasik.gamedex.util.setAll
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

/**
 * User: ykrasik
 * Date: 04/05/2019
 * Time: 20:51
 */
class OverlayPane : StackPane() {
    private val log = logger()

    @Suppress("UNCHECKED_CAST")
    private val visibleLayers = children as ObservableList<OverlayLayer>

    private val savedLayers = mutableListOf<OverlayLayer>()

    init {
        visibleProperty().bind(isNotEmpty(visibleLayers))
        addEventHandler(MouseEvent.MOUSE_CLICKED) { it.consume() }
        addEventHandler(KeyEvent.KEY_PRESSED) { it.consume() }
        visibleLayers.onChange {
            val preLast = visibleLayers.getOrNull(visibleLayers.size - 2)
            preLast?._activeProperty?.set(false)

            val last = visibleLayers.lastOrNull()
            last?._activeProperty?.set(true)
            last?.view?.root?.requestFocus()
        }
    }

    fun show(view: View, modal: Boolean, onExternalCloseRequested: () -> Unit, customizeOverlay: OverlayLayer.() -> Unit) =
        show(OverlayLayer(view, modal, onExternalCloseRequested).also(customizeOverlay))

    private fun show(layer: OverlayLayer) {
        log.trace("Showing overlay: ${layer.view}")
        layer.content.isVisible = true
        layer.content.children += layer.view.root
        visibleLayers += layer
        layer.fillTransition(0.2.seconds, from = invisibleColor, to = visibleColor)
    }

    fun isShowing(view: View): Boolean = visibleLayers.any { it.view === view }

    fun hide(view: View) = hide(checkNotNull(visibleLayers.findLast { it.view === view }) { "View not showing: $view" })

    private fun hide(layer: OverlayLayer) {
        log.trace("Hiding overlay: ${layer.view}")
        // Immediately hide the content
        layer.content.isVisible = false
        layer.content.children -= layer.view.root

        layer.isHiding = true
        layer.fillTransition(0.2.seconds, from = visibleColor, to = invisibleColor) {
            setOnFinished {
                visibleLayers -= layer
                layer.isHiding = false
            }
        }
    }

    fun hideAll() = visibleLayers.forEach(::hide)

    fun saveAndClear() {
        savedLayers.setAll(visibleLayers.filter { !it.isHiding })
        visibleLayers.toList().forEach(::hide)
    }

    fun restoreSaved() {
        savedLayers.forEach(::show)
        savedLayers.clear()
    }

    class OverlayLayer(val view: View, modal: Boolean, onExternalCloseRequested: () -> Unit) : StackPane() {
        internal val _activeProperty = SimpleBooleanProperty(false)
        val activeProperty: ReadOnlyBooleanProperty = _activeProperty
        var isActive: Boolean
            get() = _activeProperty.value
            internal set(value) {
                _activeProperty.value = value
            }

        internal val _hidingProperty = SimpleBooleanProperty(false)
        val hidingProperty: ReadOnlyBooleanProperty = _hidingProperty
        var isHiding: Boolean
            get() = _hidingProperty.value
            internal set(value) {
                _hidingProperty.value = value
            }

        init {
            // This stackPane extends over the whole screen and will be darkened.
            // It catches clicks outside of the content area and hides the content.
            useMaxSize = true
            addEventHandler(MouseEvent.MOUSE_CLICKED) {
                if (!modal) {
                    onExternalCloseRequested()
                }
                it.consume()
            }
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ESCAPE) {
                    onExternalCloseRequested()
                    it.consume()
                }
            }
            addEventHandler(KeyEvent.KEY_PRESSED) { it.consume() }
        }

        val content = stackpane {
            // This stackPane will wrap the content,
            // it's here mostly to prevent the parent stackPane from resizing the content to the whole screen.
            maxWidth = Region.USE_PREF_SIZE
            maxHeight = Region.USE_PREF_SIZE
            addClass(Style.overlayContent)
            clipRectangle(arc = 20)
            addEventHandler(MouseEvent.MOUSE_CLICKED) { it.consume() }
        }
    }

    private companion object {
        val visibleColor: Color = Color.rgb(0, 0, 0, 0.7)
        val invisibleColor: Color = Color.rgb(0, 0, 0, 0.0)
    }

    class Style : Stylesheet() {
        companion object {
            val overlayContent by cssclass()

            init {
                importStylesheetSafe(Style::class)
            }
        }

        init {
            overlayContent {
                backgroundColor = multi(Colors.cloudyKnoxville)
            }
        }
    }
}