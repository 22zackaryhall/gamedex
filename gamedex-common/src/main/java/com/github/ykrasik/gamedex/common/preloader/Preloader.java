package com.github.ykrasik.gamedex.common.preloader;

import javafx.concurrent.Task;

import java.util.function.Consumer;

/**
 * @author Yevgeny Krasik
 */
public interface Preloader {
    void info(String message);

    <T> void start(Task<T> task, Consumer<T> consumer);
}
