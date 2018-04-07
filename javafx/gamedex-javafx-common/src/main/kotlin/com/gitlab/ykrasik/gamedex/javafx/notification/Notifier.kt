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

package com.gitlab.ykrasik.gamedex.javafx.notification

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.core.api.task.Task
import com.gitlab.ykrasik.gamedex.core.api.task.TaskRunner
import com.gitlab.ykrasik.gamedex.core.provider.GameProviderRepository
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import com.gitlab.ykrasik.gamedex.util.info
import com.gitlab.ykrasik.gamedex.util.logger
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.javafx.JavaFx
import org.controlsfx.control.NotificationPane
import tornadofx.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 19/03/2018
 * Time: 22:28
 */
// TODO: This feels more like a view
@Singleton
class Notifier @Inject constructor(private val providerRepository: GameProviderRepository) : TaskRunner {
    private val log = logger()

    private val notificationPane = NotificationPane().apply {
        isCloseButtonVisible = false
        isShowFromTop = false
    }

    private var currentJob: Job? = null

    private val titleProperty = SimpleStringProperty("")
    private val progressProperty = SimpleDoubleProperty(0.0)
    private val messageProperty = SimpleStringProperty("")
    private val platformProperty = SimpleObjectProperty<Platform?>(null)
    private val providerProperty = SimpleObjectProperty<ProviderId?>(null)
    private val loadingVisibleProperty = SimpleBooleanProperty(false)

    private val taskView = GridPane().apply {
        paddingAll = 10.0
        hgap = 10.0
        vgap = 5.0
        row {
            label(titleProperty) {
                minWidth = Region.USE_COMPUTED_SIZE
            }
            progressbar(progressProperty) {
                // TODO: Add a progress counter after the progressBar.
                gridpaneConstraints { hGrow = Priority.ALWAYS }
                useMaxWidth = true
            }
            children += Theme.Images.loading.toImageView(height = 40, width = 40).apply {
                gridpaneConstraints { hAlignment = HPos.CENTER }
                visibleProperty().bind(loadingVisibleProperty)
            }
        }
        row {
            hbox(spacing = 5.0) {
                gridpaneConstraints { hAlignment = HPos.LEFT; vAlignment = VPos.CENTER }
                alignment = Pos.CENTER_LEFT
                label {
                    graphicProperty().bind(platformProperty.map { it?.toLogo(38.0) })
                }
//            imageview(providerProperty.map { it?.let { providerRepository.enabledProvider(it).logoImage.toImageView(height = 40, width = 160) } })
            }
            text(messageProperty) {
                gridpaneConstraints { hAlignment = HPos.CENTER }
            }
            cancelButton {
                isCancelButton = true
                setOnAction { currentJob!!.cancel() }
            }
        }
    }

    // FIXME: Bind the provider logo & draw sub-progress.

    fun <T : Parent> init(f: () -> T): NotificationPane {
        val node = f()
        notificationPane.content = node
        return notificationPane
    }

    // FIXME: Showing notifications causes the FontAwesome glyphs to bug up. Maybe use BootstrapFx instead?
    fun showInfoNotification(text: String) = Notification()
        .text(text)
        .information()
        .automaticallyHideAfter(3.seconds)
        .hideCloseButton()
        .position(Pos.BOTTOM_RIGHT)
        .show()

    val canRunTaskProperty = notificationPane.showingProperty().not()

    override suspend fun <T> runTask(run: suspend Task.() -> T): T = withContext(JavaFx) {
        require(currentJob == null) { "Already running a job: $currentJob" }

        val task = Task()
        var success = false
        try {
            titleProperty.bind(task.titleChannel.toObservableValue(""))
            progressProperty.bind(task.progressChannel.toObservableValue(0.0))
            messageProperty.bind(task.messageChannel.toObservableValue(""))
            messageProperty.onChange { message ->
                if (log != null) {
                    val prefix = "${platformProperty.value?.let { "[$it]" }}${providerProperty.value?.let { "[$it]" }}"
                    log.info { if (prefix.isNotEmpty()) "$prefix $message" else message!! }
                }
            }
            loadingVisibleProperty.value = true
            showPersistentNotification(taskView)

            async(CommonPool) {
                task.run()
            }.apply {
                currentJob = this
            }.await().apply {
                success = true
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
            }
            throw e
        } finally {
            currentJob = null
            hidePersistentNotification()
            loadingVisibleProperty.value = false
            progressProperty.unbind()
            messageProperty.unbind()
            platformProperty.unbind()
            providerProperty.unbind()
            task.doneMessage?.let {
                showInfoNotification(it(success))
            }
        }
    }

    fun showPersistentNotification(graphic: Node) {
        notificationPane.graphic = graphic
        notificationPane.show()
    }

    fun hidePersistentNotification() {
        notificationPane.hide()
    }
}