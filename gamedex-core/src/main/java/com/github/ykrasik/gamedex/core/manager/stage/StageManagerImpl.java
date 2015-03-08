package com.github.ykrasik.gamedex.core.manager.stage;

import com.github.ykrasik.gamedex.common.exception.RunnableThrows;
import com.github.ykrasik.gamedex.core.javafx.JavaFxUtils;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;

/**
 * @author Yevgeny Krasik
 */
@RequiredArgsConstructor
public class StageManagerImpl implements StageManager {
    @NonNull private final Stage mainStage;

    @Override
    public void runWithBlur(RunnableThrows runnable) {
        runWithBlur(mainStage, runnable);
    }

    @Override
    public void runWithBlur(Stage stage, RunnableThrows runnable) {
        callWithBlur(stage, () -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public <T> T callWithBlur(Callable<T> callable) {
        return callWithBlur(mainStage, callable);
    }

    @Override
    public <T> T callWithBlur(Stage stage, Callable<T> callable) {
        final Scene scene = stage.getScene();
        if (scene != null) {
            scene.getRoot().setEffect(new GaussianBlur());
        }

        try {
            return JavaFxUtils.callLaterIfNecessary(callable);
        } finally {
            if (scene != null) {
                scene.getRoot().setEffect(null);
            }
        }
    }
}
