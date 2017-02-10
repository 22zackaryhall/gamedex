package com.gitlab.ykrasik.gamedex.ui

import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.control.*
import tornadofx.*

/**
 * User: ykrasik
 * Date: 09/10/2016
 * Time: 16:10
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

fun areYouSureDialog(textBody: String? = null, op: (Alert.() -> Unit)? = null): Boolean {
    // TODO: TornadoFx has a built-in 'confirm' method.
    val alert = Alert(Alert.AlertType.CONFIRMATION, textBody ?: "Are You Sure?", ButtonType.OK, ButtonType.CANCEL)
    alert.headerText = "Are You Sure?"
    op?.invoke(alert)
    val buttonClicked = alert.showAndWait()

    var ok = false
    buttonClicked.ifPresent {
        when(it) {
            ButtonType.OK -> ok = true
            ButtonType.CANCEL -> ok = false
            else -> error("Unexpected buttonType: $it")
        }
    }
    return ok
}