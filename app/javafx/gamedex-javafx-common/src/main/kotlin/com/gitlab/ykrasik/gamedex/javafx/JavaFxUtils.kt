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

package com.gitlab.ykrasik.gamedex.javafx

import javafx.animation.FadeTransition
import javafx.application.Platform.runLater
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.util.Duration
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import tornadofx.*
import java.io.ByteArrayInputStream

/**
 * User: ykrasik
 * Date: 02/01/2017
 * Time: 20:45
 */
val screenBounds = Screen.getPrimary().bounds

fun javaFx(f: suspend CoroutineScope.() -> Unit): Job = launch(JavaFx, block = f)

fun runLaterIfNecessary(f: () -> Unit) = if (javafx.application.Platform.isFxApplicationThread()) {
    f()
} else {
    runLater(f)
}

fun ByteArray.toImage(): Image = Image(ByteArrayInputStream(this))
fun ByteArray.toImageView(): ImageView = this.toImage().toImageView()
fun Image.toImageView(height: Number, width: Number): ImageView = toImageView {
    fitHeight = height.toDouble()
    fitWidth = width.toDouble()
    isPreserveRatio = true
}
fun Image.toImageView(op: ImageView.() -> Unit = {}): ImageView = ImageView(this).apply { op() }

fun <S> TableView<S>.clearSelection() = selectionModel.clearSelection()

class ThreadAwareObjectProperty<T> : SimpleObjectProperty<T>() {
    override fun fireValueChangedEvent() {
        runLaterIfNecessary { super.fireValueChangedEvent() }
    }
}

class ThreadAwareStringProperty(initialValue: String? = null) : SimpleStringProperty(initialValue) {
    override fun fireValueChangedEvent() {
        runLaterIfNecessary { super.fireValueChangedEvent() }
    }
}

class ThreadAwareDoubleProperty(initialValue: Double = 0.0) : SimpleDoubleProperty(initialValue) {
    override fun fireValueChangedEvent() {
        runLaterIfNecessary { super.fireValueChangedEvent() }
    }
}

class ThreadAwareBooleanProperty(initialValue: Boolean = false) : SimpleBooleanProperty(initialValue) {
    override fun fireValueChangedEvent() {
        runLaterIfNecessary { super.fireValueChangedEvent() }
    }
}

fun Region.printSize(id: String) {
    printWidth(id)
    printHeight(id)
}

fun Region.printWidth(id: String) = printSize(id, "width", minWidthProperty(), maxWidthProperty(), prefWidthProperty(), widthProperty())
fun Region.printHeight(id: String) = printSize(id, "height", minHeightProperty(), maxHeightProperty(), prefHeightProperty(), heightProperty())

fun <S, T> TableColumnBase<S, T>.printWidth(id: String) {
    printSize(id, "width", minWidthProperty(), maxWidthProperty(), prefWidthProperty(), widthProperty())
}

fun Stage.printWidth(id: String) {
    printSize(id, "width", minWidthProperty(), maxWidthProperty(), null, widthProperty())
}

private fun printSize(id: String,
                      sizeName: String,
                      min: ObservableValue<Number>,
                      max: ObservableValue<Number>,
                      pref: ObservableValue<Number>?,
                      actual: ObservableValue<Number>) {
    println("$id min-$sizeName = ${min.value}")
    println("$id max-$sizeName = ${max.value}")
    pref?.let { println("$id pref-$sizeName = ${it.value}") }
    println("$id $sizeName = ${actual.value}")

    min.printChanges("$id min-$sizeName")
    max.printChanges("$id max-$sizeName")
    pref?.printChanges("$id pref-$sizeName")
    actual.printChanges("$id $sizeName")
}

fun ImageView.fadeOnImageChange(fadeInDuration: Duration = 0.2.seconds): ImageView = apply {
    imageProperty().onChange {
        fade(fadeInDuration, 1.0, play = true) {
            fromValue = 0.0
        }
    }
}

fun Region.padding(op: (InsetBuilder.() -> Unit)) {
    val builder = InsetBuilder(this)
    op(builder)
    padding = Insets(builder.top.toDouble(), builder.right.toDouble(), builder.bottom.toDouble(), builder.left.toDouble())
}

class InsetBuilder(region: Region) {
    var top: Number = region.padding.top
    var bottom: Number = region.padding.bottom
    var right: Number = region.padding.right
    var left: Number = region.padding.left
}

fun EventTarget.verticalSeparator(padding: Double? = 10.0, op: Separator.() -> Unit = {}) = separator(Orientation.VERTICAL, op).apply {
    padding?.let {
        padding { right = it; left = it }
    }
}

fun FontAwesome.Glyph.toGraphic(op: (Glyph.() -> Unit)? = null) = Glyph("FontAwesome", this).apply {
    op?.invoke(this)
}

fun <S> TableView<S>.allowDeselection(onClickAgain: Boolean) {
    val tableView = this
    var lastSelectedRow: TableRow<S>? = null
    setRowFactory {
        TableRow<S>().apply {
            selectedProperty().onChange {
                if (it) lastSelectedRow = this
            }
            if (onClickAgain) {
                addEventFilter(MouseEvent.MOUSE_PRESSED) { e ->
                    if (index >= 0 && index < tableView.items.size && tableView.selectionModel.isSelected(index)) {
                        tableView.selectionModel.clearSelection()
                        e.consume()
                    }
                }
            }
        }
    }
    addEventFilter(MouseEvent.MOUSE_CLICKED) { e ->
        lastSelectedRow?.let { row ->
            val boundsOfSelectedRow = row.localToScene(row.layoutBounds)
            if (!boundsOfSelectedRow.contains(e.sceneX, e.sceneY)) {
                tableView.selectionModel.clearSelection()
            }
        }
    }
}

fun Node.showWhen(expr: () -> ObservableValue<Boolean>) {
    val shouldShow = expr()
    managedWhen { shouldShow }
    visibleWhen { shouldShow }
}

fun Node.mouseTransparentWhen(expr: () -> ObservableValue<Boolean>) {
    mouseTransparentProperty().cleanBind(expr())
}

fun EventTarget.labeled(text: String, styleClasses: List<CssRule> = emptyList(), f: Pane.() -> Unit) = hbox(spacing = 5.0) {
    alignment = Pos.CENTER_LEFT
    val label = label(text) {
        styleClasses.forEach { addClass(it) }
    }
    val fakePane = Pane()
    f(fakePane)
    label.labelFor = fakePane.children.first()
    children += fakePane.children
}

fun <T> TableView<T>.minWidthFitContent(indexColumn: TableColumn<T, Number>? = null) {
    minWidthProperty().bind(contentColumns.fold(indexColumn?.widthProperty()?.subtract(10) ?: 0.0.toProperty()) { binding, column ->
        binding.add(column.widthProperty())
    })
}

fun Node.flash(duration: Duration = 0.15.seconds, target: Double = 0.0, reverse: Boolean = false): FadeTransition =
    fade(duration, if (reverse) 1.0 - target else target) {
    setOnFinished {
        fade(duration, if (reverse) 0.0 else 1.0)
    }
}