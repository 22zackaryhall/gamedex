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
package com.github.ykrasik.indexter.games.ui;

import com.github.ykrasik.indexter.games.controller.GameCollectionController;
import com.github.ykrasik.indexter.games.datamodel.LocalGameInfo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

/**
 * A {@link GridCell} that can be used to show images inside the 
 * {@link GridView} control.
 *
 * @see GridView
 */
public class GameInfoCell extends GridCell<LocalGameInfo> {

    private final ImageView imageView;

    private final boolean preserveImageProperties;


    /**
     * Creates a default ImageGridCell instance, which will preserve image properties
     */
    public GameInfoCell() {
        this(true);
    }

    /**
     * Create ImageGridCell instance
     * @param preserveImageProperties if set to true will preserve image aspect ratio and smoothness
     */
    public GameInfoCell(boolean preserveImageProperties) {
        getStyleClass().add("image-grid-cell"); //$NON-NLS-1$

        this.preserveImageProperties = preserveImageProperties;
        imageView = new ImageView();
        imageView.fitHeightProperty().bind(heightProperty());
        imageView.fitWidthProperty().bind(widthProperty());
    }

    public ImageView getImageView() {
        return imageView;
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void updateItem(LocalGameInfo item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            final Image image = item.getGameInfo().getThumbnail().orElse(GameCollectionController.NOT_AVAILABLE);
            if (preserveImageProperties) {
                imageView.setPreserveRatio(image.isPreserveRatio());
                imageView.setSmooth(image.isSmooth());
            }
            imageView.setImage(image);
            setGraphic(imageView);
        }
    }
}