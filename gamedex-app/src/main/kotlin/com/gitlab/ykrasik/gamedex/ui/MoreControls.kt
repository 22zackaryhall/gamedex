package com.gitlab.ykrasik.gamedex.ui

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.ui.widgets.FixedRatingSkin
import com.gitlab.ykrasik.gamedex.ui.widgets.ImageViewResizingPane
import com.jfoenix.controls.*
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Callback
import org.controlsfx.control.PopOver
import org.controlsfx.control.Rating
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*
import kotlin.reflect.KProperty1

/**
 * User: ykrasik
 * Date: 16/05/2017
 * Time: 09:33
 */
fun EventTarget.readOnlyTextField(value: String? = null, op: (TextField.() -> Unit)? = null) = textfield(value, op).apply {
    isEditable = false
}

fun EventTarget.readOnlyTextArea(value: String? = null, op: (TextArea.() -> Unit)? = null) = textarea(value, op).apply {
    isEditable = false
}

fun TabPane.nonClosableTab(text: String, op: (Tab.() -> Unit)? = null) = tab(text, op).apply {
    isClosable = false
}

inline fun <reified T : Enum<T>> EventTarget.enumComboBox(property: Property<T>? = null, noinline op: (ComboBox<T>.() -> Unit)? = null): ComboBox<T> {
    val enumValues = T::class.java.enumConstants.asList().observable<T>()
    return combobox(property, enumValues, op)
}

inline fun <reified S, T> TableView<S>.customColumn(title: String,
                                                    prop: KProperty1<S, T>,
                                                    crossinline cellFactory: (TableColumn<S, T>) -> TableCell<S, T>): TableColumn<S, T> {
    val column = TableColumn<S, T>(title)
    addColumnInternal(column)
    column.cellValueFactory = Callback { observable(it.value, prop) }
    column.setCellFactory { cellFactory(it) }
    return column
}

fun EventTarget.fixedRating(max: Int, isPartial: Boolean = true, op: (Rating.() -> Unit)? = null) = opcr(this, Rating(max), op).apply {
    isPartialRating = isPartial
    skin = FixedRatingSkin(this)
}

fun EventTarget.imageViewResizingPane(imageView: ImageView, op: (ImageViewResizingPane.() -> Unit)? = null) =
    opcr(this, ImageViewResizingPane(imageView), op)

fun Node.clipRectangle(op: Rectangle.() -> Unit) {
    clip = Rectangle().apply(op)
}

fun EventTarget.jfxHamburger(op: (JFXHamburger.() -> Unit)? = null) = opcr(this, JFXHamburger(), op)
fun EventTarget.jfxDrawer(op: (JFXDrawer.() -> Unit)? = null) = opcr(this, JFXDrawer(), op)
fun EventTarget.jfxToggleButton(op: (JFXToggleButton.() -> Unit)? = null) = opcr(this, JFXToggleButton(), op)
fun Node.jfxToggleNode(graphic: Node? = null, group: ToggleGroup? = getToggleGroup(), op: (JFXToggleNode.() -> Unit)? = null) = opcr(this, JFXToggleNode().apply {
    this.graphic = graphic
    this.toggleGroup = group
}, op)

fun EventTarget.jfxButton(text: String? = null, graphic: Node? = null, type: JFXButton.ButtonType = JFXButton.ButtonType.FLAT, op: (JFXButton.() -> Unit)? = null) =
    opcr(this, JFXButton().apply {
        addClass(CommonStyle.jfxButton)
        this.text = text
        this.graphic = graphic
        this.buttonType = type
    }, op)

fun EventTarget.jfxButton(text: Property<String>, graphic: Node? = null, type: JFXButton.ButtonType = JFXButton.ButtonType.FLAT, op: (JFXButton.() -> Unit)? = null) =
    jfxButton(text.value, graphic, type, op).apply {
        textProperty().cleanBind(text)
    }

fun EventTarget.acceptButton(op: (JFXButton.() -> Unit)? = null) = jfxButton(graphic = FontAwesome.Glyph.CHECK_CIRCLE_ALT.toGraphic { size(26.0); color(Color.GREEN) }).apply {
    addClass(CommonStyle.toolbarButton, CommonStyle.acceptButton)
    tooltip("Accept")
    op?.invoke(this)
}

fun EventTarget.cancelButton(op: (JFXButton.() -> Unit)? = null) = jfxButton(graphic = FontAwesome.Glyph.BAN.toGraphic { size(26.0); color(Color.RED) }).apply {
    addClass(CommonStyle.toolbarButton, CommonStyle.cancelButton)
    tooltip("Cancel")
    op?.invoke(this)
}

fun EventTarget.deleteButton(op: (JFXButton.() -> Unit)? = null) = jfxButton(graphic = FontAwesome.Glyph.TRASH.toGraphic { size(26.0); color(Color.INDIANRED) }).apply {
    addClass(CommonStyle.toolbarButton, CommonStyle.deleteButton)
    tooltip("Delete")
    op?.invoke(this)
}

fun EventTarget.buttonWithPopover(text: String? = null,
                                  graphic: Node? = null,
                                  arrowLocation: PopOver.ArrowLocation = PopOver.ArrowLocation.TOP_LEFT,
                                  styleClass: CssRule? = CommonStyle.toolbarButton,
                                  op: (PopOver.() -> Unit)? = null) =
    jfxButton(text = text, graphic = graphic) {
        styleClass?.let { addClass(it) }
        withPopover(arrowLocation) {
            op?.invoke(this)
        }
    }

// TODO: This is insane, really.
fun <T> EventTarget.popoverComboMenu(items: List<T>,
                                     arrowLocation: PopOver.ArrowLocation = PopOver.ArrowLocation.TOP_LEFT,
                                     styleClass: CssRule? = CommonStyle.toolbarButton,
                                     itemStyleClass: CssRule? = null,
                                     text: (T) -> String?,
                                     graphic: (T) -> Node?,
                                     initialSelection: T? = null,
                                     menuOp: (PopOver.(T) -> Unit)? = null,
                                     buttonOp: (JFXButton.() -> Unit)? = null): ObjectProperty<T> {
    val selectedItemProperty = SimpleObjectProperty<T>()
    buttonWithPopover(arrowLocation = arrowLocation, styleClass = styleClass) {
        items.forEach { item ->
            popoverMenuItem(text(item), graphic(item), styleClass = itemStyleClass) {
                selectedItemProperty.value = item
            }
            if (item == initialSelection) {
                selectedItemProperty.value = item
            }
            menuOp?.invoke(this, item)
        }
    }.apply {
        textProperty().bind(selectedItemProperty.map { text(it!!) })
        graphicProperty().bind(selectedItemProperty.map { graphic(it!!) })
        buttonOp?.invoke(this)
    }
    return selectedItemProperty
}

fun EventTarget.extraMenu(op: (PopOver.() -> Unit)? = null) = buttonWithPopover(
    graphic = FontAwesome.Glyph.ELLIPSIS_V.toGraphic { size(21.0) },
    arrowLocation = PopOver.ArrowLocation.TOP_RIGHT,
    op = op
)

fun PopOver.popoverMenuItem(text: String? = null,
                            graphic: Node? = null,
                            styleClass: CssRule? = CommonStyle.extraMenu,
                            onAction: () -> Unit): JFXButton {
    return popoverContent.jfxButton(text, graphic) {
        styleClass?.let { addClass(it) }
        setOnAction {
            this@popoverMenuItem.hide()
            onAction()
        }
    }
}

fun PopOver.separator(orientation: Orientation = Orientation.HORIZONTAL, op: (Separator.() -> Unit)? = null) {
    popoverContent.separator(orientation, op)
}

val PopOver.popoverContent: VBox get() {
    return if (contentNode !is VBox) {
        VBox().apply {
            addClass(CommonStyle.popoverMenu)
            contentNode = this
        }
    } else {
        contentNode as VBox
    }
}

fun <T> EventTarget.jfxComboBox(property: Property<T>? = null, values: List<T>? = null, op: (JFXComboBox<T>.() -> Unit)? = null) = opcr(this, JFXComboBox<T>().apply {
    if (values != null) items = if (values is ObservableList<*>) values as ObservableList<T> else values.observable()
    if (property != null) valueProperty().bindBidirectional(property)
}, op)

fun popOver(arrowLocation: PopOver.ArrowLocation = PopOver.ArrowLocation.TOP_LEFT, op: (PopOver.() -> Unit)? = null): PopOver =
    PopOver().apply {
        this.arrowLocation = arrowLocation
        isAnimated = false  // A ton of exceptions start getting thrown if closing a window with an open popover without this.
        isDetachable = false
        op?.invoke(this)
    }

fun Button.withPopover(arrowLocation: PopOver.ArrowLocation = PopOver.ArrowLocation.TOP_LEFT, op: (PopOver.() -> Unit)? = null) {
    val popover = popOver(arrowLocation, op)
    setOnAction {
        if (popover.isShowing) popover.hide() else popover.show(this)
    }
}

fun EventTarget.platformComboBox(property: Property<Platform>? = null,
                                 values: List<Platform>? = null,
                                 op: (ComboBox<Platform>.() -> Unit)? = null) {
    combobox(property, values) {
        setCellFactory {
            object : ListCell<Platform>() {
                override fun updateItem(item: Platform?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        text = null
                        graphic = null
                    } else {
                        text = item.toString()
                        graphic = item.toLogo()
                    }
                }
            }
        }
        buttonCell = object : ListCell<Platform>() {
            override fun updateItem(item: Platform?, empty: Boolean) {
                super.updateItem(item, empty)
                if (item == null || empty) {
                    text = null
                    graphic = null
                } else {
                    text = item.toString()
                    graphic = item.toLogo()
                }
            }
        }

        op?.invoke(this)
    }
}

fun Platform.toLogo() = when (this) {
    Platform.pc -> FontAwesome.Glyph.WINDOWS.toGraphic { color(Color.CORNFLOWERBLUE); size(19.0) }
    Platform.android -> FontAwesome.Glyph.ANDROID.toGraphic { color(Color.FORESTGREEN); size(19.0) }
    Platform.mac -> FontAwesome.Glyph.APPLE.toGraphic { color(Color.GRAY); size(19.0) }
    else -> FontAwesome.Glyph.QUESTION.toGraphic { size(19.0) }
}

fun Node.dropDownMenu(arrowLocation: PopOver.ArrowLocation = PopOver.ArrowLocation.TOP_LEFT, op: (PopOver.() -> Unit)? = null): PopOver {
    val popover = popOver(arrowLocation)
    this@dropDownMenu.setOnMouseEntered {
        if (!popover.isShowing) {
            popover.show(this@dropDownMenu)
        }
    }
    this@dropDownMenu.setOnMouseExited {
        if (!(it.screenX >= popover.x && it.screenX <= popover.x + popover.width &&
            it.screenY >= popover.y && it.screenY <= popover.y + popover.height)) {
            popover.hide()
        }
    }
    op?.invoke(popover)
    return popover
}