package com.gitlab.ykrasik.gamedex.ui.view.widgets

import javafx.geometry.HPos
import javafx.geometry.Orientation
import javafx.geometry.VPos
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane

/**
 * User: ykrasik
 * Date: 30/03/2017
 * Time: 08:36
 */
class ImageViewResizingPane(private val imageView: ImageView) : Pane() {
    init {
        children += imageView
    }

    override fun layoutChildren() {
        layoutInArea(imageView, 0.0, 0.0, imageView.fitWidth, imageView.fitHeight, 0.0, HPos.CENTER, VPos.CENTER)
    }

    override fun resize(width: Double, height: Double) {
        val image = imageView.image
        val normalizedWidth = Math.min(width, image.width * maxEnlargeRatio)
        val normalizedHeight = Math.min(height, image.height * maxEnlargeRatio)

        imageView.fitHeight = normalizedHeight
        imageView.fitWidth = normalizedWidth
        // If there are a few tiny pixel margins between the image and the border (no idea why JavaFX does this, but it does),
        // stretch the image to fill those in.
        imageView.isPreserveRatio = !(Math.abs(normalizedWidth - width) <= 10 && Math.abs(normalizedHeight - height) <= 10)

        super.resize(normalizedWidth, normalizedHeight)
    }

    override fun computePrefWidth(availableHeight: Double): Double =
        if (availableHeight <= 0) {
            availableHeight
        } else {
            availableHeight / imageRatio
        }

    override fun computePrefHeight(availableWidth: Double): Double =
        if (availableWidth <= 0) {
            availableWidth
        } else {
            availableWidth * imageRatio
        }

    override fun computeMinWidth(availableHeight: Double): Double {
        val prefWidth = computePrefWidth(availableHeight)
        return limitByMaxWidth(prefWidth)
    }

    override fun computeMinHeight(availableWidth: Double): Double {
        val prefHeight = computePrefHeight(availableWidth)
        return limitByMaxHeight(prefHeight)
    }

    override fun getContentBias(): Orientation = Orientation.VERTICAL

    private val imageRatio: Double get() {
        val image = imageView.image
        return image.height / image.width
    }

    private fun limitByMaxWidth(width: Double) = limitIfApplicable(width, maxWidth)
    private fun limitByMaxHeight(height: Double) = limitIfApplicable(height, maxHeight)
    private fun limitIfApplicable(value: Double, max: Double) = if (max > 0) Math.min(value, max) else value

    companion object {
        private val maxEnlargeRatio = 1.6    // FIXME: Property
    }
}
