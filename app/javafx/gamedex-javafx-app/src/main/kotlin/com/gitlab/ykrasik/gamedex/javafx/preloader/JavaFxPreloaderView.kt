/****************************************************************************
 * Copyright (C) 2016-2018 Yevgeny Krasik                                   *
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

package com.gitlab.ykrasik.gamedex.javafx.preloader

import com.gitlab.ykrasik.gamedex.app.api.preloader.Preloader
import com.gitlab.ykrasik.gamedex.app.api.preloader.PreloaderView
import com.gitlab.ykrasik.gamedex.javafx.EnhancedDefaultErrorHandler
import com.gitlab.ykrasik.gamedex.javafx.MainView
import com.gitlab.ykrasik.gamedex.javafx.asPercent
import com.gitlab.ykrasik.gamedex.javafx.clipRectangle
import com.gitlab.ykrasik.gamedex.javafx.module.GuiceDiContainer
import com.gitlab.ykrasik.gamedex.javafx.module.JavaFxModule
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.text.Font
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import tornadofx.*
import java.util.*

/**
 * User: ykrasik
 * Date: 01/04/2017
 * Time: 21:53
 */
class JavaFxPreloaderView : View("GameDex"), PreloaderView {
    private var logo = resources.image("gamedex.jpg")

    private val progressProperty = SimpleDoubleProperty(0.0)
    override var progress by progressProperty

    private val messageProperty = SimpleStringProperty()
    override var message by messageProperty

    override val root = stackpane {
        alignment = Pos.CENTER
        group {
            // Groups don't fill their parent's size, which is exactly what we want here.
            vbox(spacing = 5) {
                paddingAll = 5.0
                imageview {
                    image = logo

                    clipRectangle {
                        arcWidth = 10.0
                        arcHeight = 10.0
                        heightProperty().bind(logo.heightProperty())
                        widthProperty().bind(logo.widthProperty())
                    }
                }
                progressbar(progressProperty) { useMaxWidth = true }
                hbox {
                    label(messageProperty) {
                        font = Font(28.0)   // TODO: Settings this through CSS doesn't work...
                    }
                    spacer()
                    label(progressProperty.asPercent()) {
                        font = Font(28.0)   // TODO: Settings this through CSS doesn't work...
                    }
                }
            }
        }
    }

    override fun onDock() {
        primaryStage.isMaximized = true

        Thread.setDefaultUncaughtExceptionHandler(EnhancedDefaultErrorHandler())

        launch(CommonPool) {
            val preloader = ServiceLoader.load(Preloader::class.java).iterator().next()
            val injector = preloader.load(this@JavaFxPreloaderView, JavaFxModule)
            FX.dicontainer = GuiceDiContainer(injector)
            withContext(JavaFx) {
                message = "Loading user interface..."
                delay(5)       // Delay to allow the 'done' message to display.
                replaceWith(find(MainView::class), ViewTransition.Fade(2.seconds))
            }
        }
    }
}