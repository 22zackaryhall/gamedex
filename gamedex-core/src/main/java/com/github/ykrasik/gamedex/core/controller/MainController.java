package com.github.ykrasik.gamedex.core.controller;

import com.github.ykrasik.gamedex.common.util.PlatformUtils;
import com.github.ykrasik.gamedex.core.manager.game.GameManager;
import com.github.ykrasik.gamedex.core.manager.library.LibraryManager;
import com.github.ykrasik.gamedex.core.service.action.ActionService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.StatusBar;

/**
 * @author Yevgeny Krasik
 */
// TODO: Allow changing thumbnail & poster via right-click.
// TODO: Add detail view on double click
// TODO: Add right-click menus to library list.
// TODO: Photos should be streamed, and only fetched from DB when accessed. Especially posters.
// TODO: Log should be a splitPane.
// TODO: Add ability to have gamePacks.
@Slf4j
@RequiredArgsConstructor
public class MainController implements Controller {
    @FXML private MenuItem addLibraryMenuItem;

    @FXML private SplitPane content;
    private double dividerPosition;

    @FXML private VBox bottomContainer;
    @FXML private StatusBar statusBar;
    @FXML private Label gameCount;
    @FXML private Label libraryCount;
    @FXML private ToggleButton toggleLog;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button statusBarStopButton;
    @FXML private TextArea logTextArea;

    @NonNull private final ActionService actionService;
    @NonNull private final GameManager gameManager;
    @NonNull private final LibraryManager libraryManager;

    // Called by JavaFx
    public void initialize() {
        initMenu();
        initBottom();
    }

    private void initMenu() {
        addLibraryMenuItem.setOnAction(e -> actionService.addNewLibrary());
//        showSideBar.selectedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) {
//                contentScreen.getChildren().add(sideBar);
//            } else {
//                contentScreen.getChildren().remove(sideBar);
//            }
//        });
    }

    private void initBottom() {
        actionService.fetchProgressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                PlatformUtils.runLaterIfNecessary(() -> progressIndicator.setProgress(newValue.doubleValue()));
            }
        });
        actionService.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                PlatformUtils.runLaterIfNecessary(() -> statusBar.setProgress(newValue.doubleValue()));
            }
        });
        actionService.messageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                PlatformUtils.runLaterIfNecessary(() -> {
                    statusBar.setText(newValue);
                    logTextArea.appendText(newValue);
                    logTextArea.appendText("\n");
                });
            }
        });

        dividerPosition = content.getDividerPositions()[0];
        toggleLog.selectedProperty().addListener((observable, oldValue, newValue) -> toggleLogTextArea(newValue));

        gameCount.textProperty().bind(gameManager.gamesProperty().sizeProperty().asString("Games: %d"));
        libraryCount.textProperty().bind(libraryManager.librariesProperty().sizeProperty().asString("Libraries: %d"));
    }

    private void toggleLogTextArea(boolean newValue) {
        if (newValue) {
            content.getItems().add(logTextArea);
            content.setDividerPositions(dividerPosition);
        } else {
            dividerPosition = content.getDividerPositions()[0];
            content.getItems().remove(logTextArea);
        }
    }

    private void prepareTask(Task<Void> task) {
        task.setOnCancelled(event -> actionService.stopTask(task));

        progressIndicator.visibleProperty().bind(task.runningProperty());

        statusBarStopButton.disableProperty().bind(task.runningProperty().not());
        statusBarStopButton.visibleProperty().bind(task.runningProperty());
        statusBarStopButton.setOnAction(e -> task.cancel());

        // TODO: Disable all other buttons while task is running.
    }
}
