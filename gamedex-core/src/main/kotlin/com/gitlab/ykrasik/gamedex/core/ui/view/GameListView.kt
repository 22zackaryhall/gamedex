package com.gitlab.ykrasik.gamedex.core.ui.view

import com.github.ykrasik.gamedex.datamodel.Game
import com.gitlab.ykrasik.gamedex.core.ui.controller.GameController
import com.gitlab.ykrasik.gamedex.core.ui.dividerPosition
import com.gitlab.ykrasik.gamedex.core.ui.model.GameRepository
import com.gitlab.ykrasik.gamedex.core.ui.padding
import com.gitlab.ykrasik.gamedex.core.ui.readOnlyTextArea
import com.gitlab.ykrasik.gamedex.core.ui.readOnlyTextField
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.Priority
import tornadofx.*

/**
 * User: ykrasik
 * Date: 09/10/2016
 * Time: 15:06
 */
class GameListView : View("Game List") {
    private val controller: GameController by di()
    private val gameRepository: GameRepository by di()

    override val root = splitpane {
        dividerPosition = 0.69

        tableview<Game> {
            itemsProperty().bind(gameRepository.gamesProperty)

            isEditable = false
            columnResizePolicy = SmartResize.POLICY

            column("Name", Game::name) {
//                        prefWidth = -1.0
                isSortable = false
                contentWidth(100.0, useAsMin = true)
                remainingWidth()
            }
            column("Critic Score", Game::criticScore) {
//                        prefWidth = -1.0
                isSortable = false
                contentWidth(10.0, useAsMin = true)
            }
            column("User Score", Game::userScore) {
//                        prefWidth = -1.0
                isSortable = false
                contentWidth(10.0, useAsMin = true)
            }
            column("Release Date", Game::releaseDate) {
//                        prefWidth = -1.0
                isSortable = false
                contentWidth(10.0, useAsMin = true)
            }
            column("Path", Game::path) {
//                        prefWidth = -1.0
                isSortable = false
                contentWidth(400.0, useAsMin = true)
            }

            contextmenu {
                menuitem("Delete") { selectedItem?. let { controller.delete(it) }}
            }
        }

        gridpane {
            gridpaneConstraints {
                hGrow = Priority.ALWAYS
                minWidth = 10.0

                vGrow = Priority.ALWAYS
                vAlignment = VPos.BOTTOM    // Or center?
                minHeight = 10.0
                fillHeight = false
            }
            gridpane {
                hgap = 3.0
                vgap = 3.0
                alignment = Pos.BOTTOM_CENTER
                gridpaneConstraints {
                    columnIndex = 1

                    hAlignment = HPos.LEFT  // Or Right?
                    hGrow = Priority.SOMETIMES
                    fillWidth = false
                    minWidth = Double.NEGATIVE_INFINITY

                    vGrow = Priority.SOMETIMES  // Or Always?
                    fillHeight = false
                    minHeight = Double.NEGATIVE_INFINITY
                    maxHeight = 60.0
                }

                form {
                    imageview {
                        isPreserveRatio = true
                        isPickOnBounds = true

                        padding { bottom = 5.0; left = 5.0; right = 5.0 }
                    }
                    fieldset("Details") {
                        field("Path") { readOnlyTextField() }
                        field("Name") { readOnlyTextField() }
                        field("Description") { readOnlyTextArea { isWrapText = true } }
                        field("Release Date") { readOnlyTextField() }
                        field("Critic Score") { readOnlyTextField() }
                        field("User Score") { readOnlyTextField() }
                        field("Genres") { readOnlyTextField() }
                        field("URL") { hyperlink() }
                    }
                }
            }
        }
    }
}