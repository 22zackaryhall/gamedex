/****************************************************************************
 * Copyright (C) 2016-2019 Yevgeny Krasik                                   *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package com.gitlab.ykrasik.gamedex.app.javafx.image

import com.gitlab.ykrasik.gamedex.app.api.image.ImageGalleryView
import com.gitlab.ykrasik.gamedex.app.api.image.ImageType
import com.gitlab.ykrasik.gamedex.app.api.image.ViewImageParams
import com.gitlab.ykrasik.gamedex.app.api.util.channel
import com.gitlab.ykrasik.gamedex.app.javafx.common.JavaFxCommonOps
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.theme.Colors
import com.gitlab.ykrasik.gamedex.javafx.view.PresentableView
import com.gitlab.ykrasik.gamedex.util.IsValid
import javafx.scene.paint.Color
import tornadofx.*

/**
 * User: ykrasik
 * Date: 12/05/2019
 * Time: 09:05
 */
class JavaFxImageGalleryView : PresentableView(), ImageGalleryView {
    private val commonOps: JavaFxCommonOps by di()

    override val imageParams = userMutableState(ViewImageParams(imageUrl = "", imageUrls = emptyList(), imageType = ImageType.Thumbnail))
    override val currentImageIndex = state(-1)

    override val canViewNextImage = state(IsValid.valid)
    override val viewNextImageActions = channel<Unit>()

    override val canViewPrevImage = state(IsValid.valid)
    override val viewPrevImageActions = channel<Unit>()

    private var slideDirection = ViewTransition.Direction.LEFT

    init {
        register()
    }

    override val root = stackpane {
        children += imageView(imageParams.value)

        imageParams.property.typeSafeOnChange { params ->
            val new = imageView(params)
            children[0].replaceWith(new, ViewTransition.Slide(0.1.seconds, slideDirection))
        }
    }

    private fun imageView(params: ViewImageParams) = imageview {
        fitWidth = screenBounds.width * 3 / 4
        fitHeight = screenBounds.height * 3 / 4
        isPreserveRatio = true

        if (params.imageUrl.isNotBlank()) {
            imageProperty().bind(commonOps.fetchImage(params.imageUrl, params.imageType))
        } else {
            image = null
        }
    }

    class Style : Stylesheet() {
        companion object {
            val arrowLeftContainer by cssclass()
            val arrowLeft by cssclass()
            val arrowRightContainer by cssclass()
            val arrowRight by cssclass()

            init {
                importStylesheetSafe(Style::class)
            }
        }

        init {
            arrowLeftContainer {
                padding = box(20.px)
            }
            arrowLeft {
                backgroundColor = multi(Colors.transparentWhite)
                and(hover) {
                    backgroundColor = multi(Color.WHITE)
                }
            }
            arrowRightContainer {
                padding = box(20.px)
            }
            arrowRight {
                backgroundColor = multi(Colors.transparentWhite)
                and(hover) {
                    backgroundColor = multi(Color.WHITE)
                }
            }
        }
    }
}