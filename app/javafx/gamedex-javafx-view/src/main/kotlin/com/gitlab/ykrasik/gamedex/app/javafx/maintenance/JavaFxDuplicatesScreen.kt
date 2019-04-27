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

package com.gitlab.ykrasik.gamedex.app.javafx.maintenance

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.file.ViewCanBrowsePath
import com.gitlab.ykrasik.gamedex.app.api.game.ViewCanShowGameDetails
import com.gitlab.ykrasik.gamedex.app.api.maintenance.DuplicatesView
import com.gitlab.ykrasik.gamedex.app.api.maintenance.GameDuplicates
import com.gitlab.ykrasik.gamedex.app.api.util.channel
import com.gitlab.ykrasik.gamedex.app.javafx.common.JavaFxCommonOps
import com.gitlab.ykrasik.gamedex.app.javafx.game.GameContextMenu
import com.gitlab.ykrasik.gamedex.app.javafx.game.details.GameDetailsPaneBuilder
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.control.*
import com.gitlab.ykrasik.gamedex.javafx.theme.Icons
import com.gitlab.ykrasik.gamedex.javafx.theme.backButton
import com.gitlab.ykrasik.gamedex.javafx.theme.header
import com.gitlab.ykrasik.gamedex.javafx.theme.size
import com.gitlab.ykrasik.gamedex.javafx.view.PresentableScreen
import javafx.geometry.Orientation
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File

/**
 * User: ykrasik
 * Date: 26/04/2019
 * Time: 08:07
 */
class JavaFxDuplicatesScreen : PresentableScreen("Duplicates", Icons.copy),
    DuplicatesView,
    ViewCanShowGameDetails,
    ViewCanBrowsePath {

    private val gameContextMenu: GameContextMenu by inject()
    private val commonOps: JavaFxCommonOps by di()

    override val duplicates = mutableListOf<GameDuplicates>().observable()
//    override val excludeGameActions = channel<Game>()

    override val showGameDetailsActions = channel<Game>()

    override val searchText = userMutableState("")
    override val matchingGame = state<Game?>(null)

    override val browsePathActions = channel<File>()
//    private val browseUrlActions = channel<String>()

    override val hideViewActions = channel<Unit>()
    override val customNavigationButton = backButton { action(hideViewActions) }

    private val gamesView = customListView(duplicates) {
        vgrow = Priority.ALWAYS
        useMaxSize = true

        customListCell { duplicate ->
            val game = duplicate.game
            text = null
            maxWidth = 600.0
            graphic = GameDetailsPaneBuilder(
                name = game.name,
                nameOp = { isWrapText = true },
                platform = game.platform,
                releaseDate = game.releaseDate,
                criticScore = game.criticScore,
                userScore = game.userScore,
                path = game.path,
                fileTree = game.fileTree,
                image = commonOps.fetchThumbnail(game),
                browsePathActions = browsePathActions,
                pathOp = { isMouseTransparent = true },
                imageFitHeight = 70,
                imageFitWidth = 70,
                orientation = Orientation.HORIZONTAL
            ).build()
        }

        gameContextMenu.install(this) { selectionModel.selectedItem.game }
        onUserSelect { showGameDetailsActions.event(it.game) }
    }

    private val selectedDuplicate = gamesView.selectionModel.selectedItemProperty()
    private val duplicatesOfSelectedGame = selectedDuplicate.mapToList { it?.duplicates ?: emptyList() }

    private val duplicatesView = customListView(duplicatesOfSelectedGame) {
        vgrow = Priority.ALWAYS

        customListCell { duplicate ->
            text = null
            graphic = HBox().apply {
                spacing = 20.0
                addClass(Style.duplicateItem)
                hbox {
                    minWidth = 160.0
                    children += commonOps.providerLogo(duplicate.providerId).toImageView(height = 80.0, width = 160.0)
                }
                vbox {
                    header(duplicate.game.name) {
                        useMaxSize = true
                        hgrow = Priority.ALWAYS
                    }
                    spacer()
                    label(duplicate.game.path.toString(), Icons.folder.size(20))
                }
            }
        }
        selectionModel.selectedItemProperty().typeSafeOnChange {
            if (it != null) {
                selectGame(it.game)
            }
        }
    }

    override val root = hbox {
        vgrow = Priority.ALWAYS
        useMaxSize = true

        // Left
        vbox {
            minWidth = width
            hgrow = Priority.ALWAYS
            add(gamesView)
        }

        // Right
        vbox {
            hgrow = Priority.ALWAYS
            add(duplicatesView)
        }
    }

    init {
        register()

        matchingGame.property.typeSafeOnChange { match ->
            if (match != null) {
                selectGame(match)
            }
        }
    }

    private fun selectGame(game: Game) = runLater {
        gamesView.selectionModel.select(duplicates.indexOfFirst { it.game.id == game.id })
    }

    override fun HBox.buildToolbar() {
        searchTextField(this@JavaFxDuplicatesScreen, searchText.property) { isFocusTraversable = false }
//        gap()
//        excludeButton {
//            action(excludeGameActions) { selectedDuplicate.value!!.game }
//            tooltip(selectedDuplicate.stringBinding { duplicate ->
//                "Exclude game '${duplicate?.game?.name}' from duplicates report."
//            })
//        }
        spacer()
        header(duplicates.sizeProperty.stringBinding { "Duplicates: $it" })
        gap()
    }

    class Style : Stylesheet() {
        companion object {
            val duplicateItem by cssclass()

            init {
                importStylesheetSafe(Style::class)
            }
        }

        init {
            duplicateItem {
                fontSize = 18.px
            }
        }
    }
}