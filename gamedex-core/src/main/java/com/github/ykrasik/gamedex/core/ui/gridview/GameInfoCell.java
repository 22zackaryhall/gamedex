/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.ykrasik.gamedex.core.ui.gridview;

import com.github.ykrasik.gamedex.core.service.image.ImageService;
import com.github.ykrasik.gamedex.core.ui.UIResources;
import com.github.ykrasik.gamedex.datamodel.persistence.Game;
import com.github.ykrasik.opt.Opt;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import lombok.NonNull;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

/**
 * A {@link GridCell} that can be used to show images inside the 
 * {@link GridView} control.
 *
 * @see GridView
 */
public class GameInfoCell extends GridCell<Game> {
    private static final double STRETCH_MARGIN = 1.2;

    private final ImageView imageView = new ImageView();
    private final StackPane stackPane = new StackPane(imageView);

    private final ImageService imageService;

    private Opt<Task<Image>> loadingTask = Opt.absent();

    /**
     * Create ImageGridCell instance
     */
    public GameInfoCell(@NonNull ImageService imageService) {
        getStyleClass().add("image-grid-cell"); //$NON-NLS-1$

        this.imageService = imageService;

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Add a listener that will clip the cell's corners to be round, after the cell's height is calculated.
        final ChangeListener<Number> clipListener = (observable, oldValue, newValue) -> {
            final Rectangle clip = new Rectangle(1, 1, getWidth() - 4, getHeight() - 4);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            stackPane.setClip(clip);
        };
        heightProperty().addListener(clipListener);
        widthProperty().addListener(clipListener);

        imageView.imageProperty().addListener((observable, oldValue, newValue) -> {
            // Allow stretching the image by up to 20% if it fills up the cell.
            if (newValue != UIResources.imageLoading() &&
                (newValue.getHeight() * STRETCH_MARGIN >= getHeight() ||
                 newValue.getWidth() * STRETCH_MARGIN >= getWidth())) {
                imageView.setPreserveRatio(false);
            } else {
                imageView.setPreserveRatio(true);
            }
        });

        imageView.fitHeightProperty().bind(heightProperty().subtract(2));
        imageView.fitWidthProperty().bind(widthProperty().subtract(2));

        itemProperty().addListener((obs, oldItem, newItem) -> {
//            cancelPrevTask();

            if (newItem != null) {
                fetchImage(newItem);
                setGraphic(stackPane);
            } else {
                setGraphic(null);
                setText(null);
            }
        });
    }

    private void fetchImage(Game game) {
        loadingTask = Opt.of(imageService.fetchThumbnail(game.getId(), imageView));
    }

    private void cancelPrevTask() {
        if (loadingTask.isPresent()) {
            final Task<Image> task = loadingTask.get();
            if (task.getState() != Worker.State.SUCCEEDED &&
                task.getState() != Worker.State.FAILED) {
                task.cancel();
            }
        }
        loadingTask = Opt.absent();
    }
}