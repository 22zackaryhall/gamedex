package com.github.ykrasik.gamedex.core.service.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Yevgeny Krasik
 */
public final class DialogFactory {
    private DialogFactory() { }

    public static Alert createExceptionDialog(Throwable t) {
        final Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText(t.getMessage());

        final StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));
        final String exceptionText = stringWriter.toString();

        final TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        return alert;
    }

    public static <T> Alert createConfirmationListDialog(String text, ObservableList<T> list, Function<T, String> stringifier) {
        final Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText(text);

        final ObservableList<String> stringList = FXCollections.observableArrayList(list.stream().map(stringifier::apply).collect(Collectors.toList()));
        final ListView<String> listView = new ListView<>(stringList);
        alert.getDialogPane().setContent(listView);
        return alert;
    }
}