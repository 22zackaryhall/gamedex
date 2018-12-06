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

import com.gitlab.ykrasik.gamedex.javafx.CommonStyle
import com.jfoenix.controls.JFXButton
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.VBox
import org.controlsfx.control.PopOver
import tornadofx.action
import tornadofx.addClass
import tornadofx.opcr

/**
 * User: ykrasik
 * Date: 09/12/2018
 * Time: 13:22
 */
inline fun EventTarget.jfxButton(
    text: String? = null,
    graphic: Node? = null,
    type: JFXButton.ButtonType = JFXButton.ButtonType.FLAT,
    alignment: Pos = Pos.CENTER,
    op: JFXButton.() -> Unit = {}
) = opcr(this, JFXButton().apply {
    addClass(CommonStyle.jfxHoverable)
    this.text = text
    this.graphic = graphic
    this.buttonType = type
    this.alignment = alignment
}, op)

inline fun EventTarget.buttonWithPopover(
    text: String? = null,
    graphic: Node? = null,
    arrowLocation: PopOver.ArrowLocation = PopOver.ArrowLocation.TOP_LEFT,
    closeOnClick: Boolean = true,
    op: VBox.(PopOver) -> Unit = {}
) = jfxButton(text = text, graphic = graphic, alignment = Pos.CENTER_LEFT) {
    val popover = popOver(arrowLocation, closeOnClick, op)
    action { popover.toggle(this) }
}