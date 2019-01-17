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

import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.util.IsValid
import com.gitlab.ykrasik.gamedex.util.asPercent
import com.jfoenix.controls.*
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.shape.Rectangle
import org.controlsfx.control.MaskerPane
import tornadofx.*

/**
 * User: ykrasik
 * Date: 16/05/2017
 * Time: 09:33
 */
inline fun Node.clipRectangle(op: Rectangle.() -> Unit) {
    clip = Rectangle().apply(op)
}

fun Region.clipRectangle(arc: Number) = clipRectangle {
    arcHeight = arc.toDouble()
    arcWidth = arc.toDouble()
    heightProperty().bind(this@clipRectangle.heightProperty())
    widthProperty().bind(this@clipRectangle.widthProperty())
}

fun <T> ListView<T>.fitAtMost(numItems: Int) {
    val size = itemsProperty().doubleBinding { minOf(it!!.size, numItems) * 24.1 }
    minHeightProperty().bind(size)
    maxHeightProperty().bind(size)
}

inline fun StackPane.maskerPane(op: MaskerPane.() -> Unit = {}) = opcr(this, MaskerPane(), op)
inline fun StackPane.maskerPane(visible: BooleanProperty, op: MaskerPane.() -> Unit = {}) = maskerPane {
    visibleWhen { visible }
    op()
}

inline fun View.skipFirstTime(op: () -> Unit) {
    val skip = properties.getOrDefault("Gamedex.skipFirstTime", true) as Boolean
    if (skip) {
        properties["Gamedex.skipFirstTime"] = false
    } else {
        op()
    }
}

@Suppress("UNCHECKED_CAST")
fun EventTarget.imageview(image: ObservableValue<Image>, op: ImageView.() -> Unit = {}) =
    imageview(image as ObservableValue<Image?>, op)

fun ObservableValue<out Number>.asPercent() = stringBinding { (it ?: 0).toDouble().asPercent() }

fun Region.enableWhen(isValid: JavaFxState<IsValid, SimpleObjectProperty<IsValid>>, wrapInErrorTooltip: Boolean = true): Unit =
    enableWhen(isValid.property, wrapInErrorTooltip)

fun Region.enableWhen(isValid: ObservableValue<IsValid>, wrapInErrorTooltip: Boolean = true) {
    enableWhen { isValid.binding { it.isSuccess } }
    if (wrapInErrorTooltip) {
        useMaxWidth = true
        hgrow = Priority.ALWAYS
        wrapInHbox {
            // Using a binding here was buggy!!!! The binding wasn't always getting called.
            errorTooltip(isValid.map { it.errorText() })
        }
    }
}

fun Field.enableWhen(isValid: JavaFxState<IsValid, SimpleObjectProperty<IsValid>>): Unit = enableWhen(isValid.property)
fun Field.enableWhen(isValid: ObservableValue<IsValid>) {
    val enabled = isValid.booleanBinding { it?.isSuccess ?: false }
    label.enableWhen { enabled }
    inputContainer.enableWhen { enabled }
    labelContainer.errorTooltip(isValid.stringBinding { it?.errorOrNull?.message })
}

fun Node.showWhen(isValid: JavaFxState<IsValid, SimpleObjectProperty<IsValid>>): Unit = showWhen { isValid.property.booleanBinding { it?.isSuccess ?: false } }
fun Node.showWhen(expr: () -> ObservableValue<Boolean>) {
    val shouldShow = expr()
    managedWhen { shouldShow }
    visibleWhen { shouldShow }
}

inline fun Node.wrapInHbox(crossinline op: HBox.() -> Unit = {}) {
    val wrapper = HBox().apply {
        alignment = Pos.CENTER_LEFT
        op()
    }
    parentProperty().perform {
        if (it !is Pane || it == wrapper) return@perform

        val index = it.children.indexOf(this)
        it.children.removeAt(index)
        wrapper.children += this
        it.children.add(index, wrapper)
    }
}

fun JavaFxState<IsValid, SimpleObjectProperty<IsValid>>.errorText(): ObservableValue<String?> =
    property.stringBinding { it?.errorText() }

fun IsValid.errorText(): String? = errorOrNull?.message

inline fun Parent.gap(size: Number = 20, f: Region.() -> Unit = {}) =
    region { minWidth = size.toDouble() }.also(f)

inline fun Parent.verticalGap(size: Number = 10, f: Region.() -> Unit = {}) =
    region { minHeight = size.toDouble() }.also(f)

fun EventTarget.defaultHbox(spacing: Number = 5, alignment: Pos = Pos.CENTER_LEFT, op: HBox.() -> Unit = {}) =
    hbox(spacing, alignment, op)

inline fun Fieldset.horizontalField(text: String? = null, forceLabelIndent: Boolean = false, crossinline op: Field.() -> Unit = {}) =
    field(text, forceLabelIndent = forceLabelIndent) {
        (inputContainer as HBox).alignment = Pos.CENTER_LEFT
        op()
    }

inline fun EventTarget.jfxTabPane(op: JFXTabPane.() -> Unit = {}): JFXTabPane =
    opcr(this, JFXTabPane()) {
        doNotConsumeMouseEvents()
        op()
    }

inline fun <T> EventTarget.jfxListView(values: ObservableList<T>, op: JFXListView<T>.() -> Unit = {}) =
    opcr(this, JFXListView<T>()) {
        if (values is SortedFilteredList<T>) {
            values.bindTo(this)
        } else {
            items = values
        }
        op()
    }

inline fun EventTarget.jfxProgressBar(op: JFXProgressBar.() -> Unit = {}): JFXProgressBar =
    opcr(this, JFXProgressBar(), op)

inline fun EventTarget.jfxProgressBar(progress: ObservableValue<out Number>, op: JFXProgressBar.() -> Unit = {}): JFXProgressBar =
    jfxProgressBar {
        progressProperty().bind(progress)
        op()
    }

inline fun EventTarget.jfxSpinner(op: JFXSpinner.() -> Unit = {}): JFXSpinner =
    opcr(this, JFXSpinner(), op)

inline fun <T> EventTarget.jfxTreeView(op: JFXTreeView<T>.() -> Unit): JFXTreeView<T> =
    opcr(this, JFXTreeView(), op)

fun <T : Node> EventTarget.add(child: T, op: T.() -> Unit) = add(child.apply { op() })