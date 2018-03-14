package com.gitlab.ykrasik.gamedex.ui.view.settings

import com.gitlab.ykrasik.gamedex.controller.SettingsController
import com.gitlab.ykrasik.gamedex.ui.jfxButton
import com.gitlab.ykrasik.gamedex.ui.theme.Theme
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

/**
 * User: ykrasik
 * Date: 05/06/2017
 * Time: 14:57
 */
class GeneralSettingsView : View("General Settings", Theme.Icon.settings()) {
    private val settingsController: SettingsController by di()

    override val root = vbox {
        group("Database") {
            row {
                jfxButton("Export Database", Theme.Icon.upload()) {
                    addClass(Style.settingsButton, Style.exportButton)
                    useMaxWidth = true
                    alignment = Pos.CENTER_LEFT
                    enableWhen { settingsController.canRunLongTask }
                    setOnAction { settingsController.exportDatabase() }
                }
            }
            row {
                jfxButton("Import Database", Theme.Icon.download()) {
                    addClass(Style.settingsButton, Style.importButton)
                    useMaxWidth = true
                    alignment = Pos.CENTER_LEFT
                    enableWhen { settingsController.canRunLongTask }
                    setOnAction { settingsController.importDatabase() }
                }
            }
            row {
                region { prefHeight = 20.0 }
            }
            row {
                jfxButton("Cleanup", Theme.Icon.delete(color = Color.RED)) {
                    addClass(Style.settingsButton, Style.cleanupDbButton)
                    useMaxWidth = true
                    alignment = Pos.CENTER_LEFT
                    enableWhen { settingsController.canRunLongTask }
                    setOnAction { settingsController.cleanupDb() }
                }
            }
        }
    }

    private fun EventTarget.group(title: String, op: GridPane.() -> Unit = {}): GridPane {
        label(title) { addClass(Style.title) }
        return gridpane {
            paddingTop = 5.0
            vgap = 5.0
            op()
        }
    }

    class Style : Stylesheet() {
        companion object {
            val title by cssclass()
            val settingsButton by cssclass()
            val importButton by cssclass()
            val exportButton by cssclass()
            val cleanupDbButton by cssclass()

            init {
                importStylesheet(Style::class)
            }
        }

        init {
            title {
                fontSize = 14.px
                fontWeight = FontWeight.BOLD
            }

            settingsButton {
                borderColor = multi(box(Color.BLACK))
                borderRadius = multi(box(3.px))
                borderWidth = multi(box(0.5.px))
            }

            importButton {
                and(hover) {
                    backgroundColor = multi(Color.CORNFLOWERBLUE)
                }
            }

            exportButton {
                and(hover) {
                    backgroundColor = multi(Color.LIMEGREEN)
                }
            }

            cleanupDbButton {
                and(hover) {
                    backgroundColor = multi(Color.INDIANRED)
                }
            }
        }
    }
}