package com.github.ykrasik.gamedex.core.service.screen.settings;

import com.github.ykrasik.gamedex.core.manager.stage.StageManager;
import com.github.ykrasik.gamedex.core.service.config.ConfigService;
import com.github.ykrasik.gamedex.core.ui.UIResources;
import com.github.ykrasik.gamedex.core.ui.gridview.GameWallImageDisplay;
import com.github.ykrasik.yava.javafx.JavaFxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * @author Yevgeny Krasik
 */
public class SettingsScreen {
    private final Stage stage = new Stage();

    private final ConfigService configService;
    private final StageManager stageManager;

    @FXML private ComboBox<GameWallImageDisplay> gameWallImageDisplayComboBox;

    @SneakyThrows
    public SettingsScreen(@NonNull ConfigService configService, @NonNull StageManager stageManager) {
        this.configService = configService;
        this.stageManager = stageManager;

        final FXMLLoader loader = new FXMLLoader(UIResources.settingsScreenFxml());
        loader.setController(this);
        final BorderPane root = loader.load();

        final Scene scene = new Scene(root, Color.TRANSPARENT);
        scene.getStylesheets().addAll(UIResources.mainCss(), UIResources.settingsScreenCss());

        stage.setWidth(600);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);

        // Make the stage draggable by clicking anywhere.
        JavaFxUtils.makeStageDraggable(stage, root);
    }

    @FXML
    private void initialize() {
        initGameWallImageDisplay();
    }

    private void initGameWallImageDisplay() {
        gameWallImageDisplayComboBox.setItems(FXCollections.observableArrayList(GameWallImageDisplay.values()));

        gameWallImageDisplayComboBox.getSelectionModel().select(configService.getGameWallImageDisplay());
        configService.gameWallImageDisplayProperty().bind(gameWallImageDisplayComboBox.getSelectionModel().selectedItemProperty());
    }

    public void show() {
        stageManager.runWithBlur(stage::showAndWait);
    }

    @FXML
    public void close() {
        stage.close();
    }
}