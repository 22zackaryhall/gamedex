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

package com.gitlab.ykrasik.gamedex.javafx.control

import com.gitlab.ykrasik.gamedex.javafx.theme.CommonStyle
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXToggleButton
import com.jfoenix.controls.JFXToggleNode
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import tornadofx.addClass
import tornadofx.getToggleGroup
import tornadofx.opcr
import tornadofx.useMaxWidth
import kotlin.collections.set

/**
 * User: ykrasik
 * Date: 09/12/2018
 * Time: 13:24
 */
inline fun EventTarget.jfxToggleButton(op: JFXToggleButton.() -> Unit = {}) =
    opcr(this, JFXToggleButton(), op)

inline fun EventTarget.jfxToggleButton(
    property: Property<Boolean>,
    text: String? = null,
    crossinline op: JFXToggleButton.() -> Unit = {}
) = jfxToggleButton {
    selectedProperty().bindBidirectional(property)
    this.text = text
    op(this)
}

inline fun Node.jfxToggleNode(
    text: String? = null,
    graphic: Node? = null,
    group: ToggleGroup? = getToggleGroup(),
    op: JFXToggleNode.() -> Unit = {}
) = opcr(this, JFXToggleNode()) {
    addClass(CommonStyle.jfxHoverable)
    this.graphic = Label(text, graphic).apply {
        addClass(CommonStyle.jfxToggleNodeLabel)
        useMaxWidth = true
    }
    this.toggleGroup = group
    op()
}

inline fun Node.jfxToggleNode(
    text: String? = null,
    graphic: Node? = null,
    value: Any? = null,
    group: ToggleGroup? = getToggleGroup(),
    op: JFXToggleNode.() -> Unit = {}
): JFXToggleNode = jfxToggleNode(text, graphic, group) {
    toggleValue(value)
    op()
}

fun ToggleButton.toggleValue(value: Any?) = apply {
    properties["tornadofx.toggleGroupValue"] = value
}

inline fun EventTarget.jfxCheckBox(op: JFXCheckBox.() -> Unit = {}) = opcr(this, JFXCheckBox(), op)

inline fun EventTarget.jfxCheckBox(
    property: Property<Boolean>,
    text: String? = null,
    crossinline op: JFXCheckBox.() -> Unit = {}
) = jfxCheckBox {
    selectedProperty().bindBidirectional(property)
    this.text = text
    op(this)
}

fun ToggleGroup.disallowDeselection() = apply {
    selectedToggleProperty().addListener { _, oldValue, newValue ->
        if (newValue == null) {
            selectToggle(oldValue)
        }
    }
}