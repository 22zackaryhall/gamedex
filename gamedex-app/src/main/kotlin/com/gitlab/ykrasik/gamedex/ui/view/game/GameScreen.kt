package com.gitlab.ykrasik.gamedex.ui.view.game

import com.gitlab.ykrasik.gamedex.controller.GameController
import com.gitlab.ykrasik.gamedex.controller.LibraryController
import com.gitlab.ykrasik.gamedex.settings.GameSettings
import com.gitlab.ykrasik.gamedex.ui.*
import com.gitlab.ykrasik.gamedex.ui.theme.CommonStyle
import com.gitlab.ykrasik.gamedex.ui.theme.Theme
import com.gitlab.ykrasik.gamedex.ui.theme.deleteButton
import com.gitlab.ykrasik.gamedex.ui.theme.toLogo
import com.gitlab.ykrasik.gamedex.ui.view.GamedexScreen
import com.gitlab.ykrasik.gamedex.ui.view.game.list.GameListView
import com.gitlab.ykrasik.gamedex.ui.view.game.menu.GameFilterMenu
import com.gitlab.ykrasik.gamedex.ui.view.game.menu.GameRefreshMenu
import com.gitlab.ykrasik.gamedex.ui.view.game.menu.GameSearchMenu
import com.gitlab.ykrasik.gamedex.ui.view.game.wall.GameWallView
import javafx.event.EventTarget
import javafx.scene.control.TableColumn
import javafx.scene.control.ToolBar
import tornadofx.*

/**
 * User: ykrasik
 * Date: 09/10/2016
 * Time: 22:14
 */
class GameScreen : GamedexScreen("Games", Theme.Icon.games()) {
    private val gameController: GameController by di()
    private val libraryController: LibraryController by di()
    private val settings: GameSettings by di()

    private val gameWallView: GameWallView by inject()
    private val gameListView: GameListView by inject()

    private val filterMenu: GameFilterMenu by inject()
    private val searchMenu: GameSearchMenu by inject()
    private val refreshMenu: GameRefreshMenu by inject()

    // FIXME: Change search -> sync, refresh maybe to download?
    override fun ToolBar.constructToolbar() {
        platformButton()
        verticalSeparator()
        items += filterMenu.root
        verticalSeparator()
        sortButton()
        verticalSeparator()

        spacer()

        verticalSeparator()
        items += searchMenu.root
        verticalSeparator()
        items += refreshMenu.root
        verticalSeparator()
        cleanupButton()
        verticalSeparator()
    }

    override val root = stackpane()

    init {
        settings.displayTypeProperty.perform {
            root.replaceChildren(it!!.toNode())
        }
    }

    private fun EventTarget.platformButton() {
        val platformsWithLibraries = libraryController.realLibraries.mapping { it.platform }.distincting()
        popoverComboMenu(
            possibleItems = platformsWithLibraries,
            selectedItemProperty = settings.platformProperty,
            styleClass = CommonStyle.toolbarButton,
            text = { it.key },
            graphic = { it.toLogo(26.0) }
        ).apply {
            textProperty().cleanBind(gameController.sortedFilteredGames.sizeProperty().stringBinding { "Games: $it" })
            mouseTransparentWhen { platformsWithLibraries.mapProperty { it.size <= 1 } }
        }
    }

    private fun EventTarget.sortButton() {
        val possibleItems = settings.sortProperty.mapToList { sort ->
            GameSettings.SortBy.values().toList().map { sortBy ->
                GameSettings.Sort(
                    sortBy = sortBy,
                    order = if (sortBy == sort.sortBy) sort.order.toggle() else TableColumn.SortType.DESCENDING
                )
            }
        }

        popoverComboMenu(
            possibleItems = possibleItems,
            selectedItemProperty = settings.sortProperty,
            styleClass = CommonStyle.toolbarButton,
            text = { it.sortBy.key },
            graphic = { it.order.toGraphic() }
        )
    }

    private fun EventTarget.cleanupButton() = deleteButton("Cleanup") {
        addClass(CommonStyle.toolbarButton)
        enableWhen { gameController.canRunLongTask }
        setOnAction { gameController.cleanup() }
    }

    private fun GameSettings.DisplayType.toNode() = when (this) {
        GameSettings.DisplayType.wall -> gameWallView.root
        GameSettings.DisplayType.list -> gameListView.root
    }

    private fun TableColumn.SortType.toGraphic() = when (this) {
        TableColumn.SortType.ASCENDING -> Theme.Icon.ascending()
        TableColumn.SortType.DESCENDING -> Theme.Icon.descending()
    }

    private fun TableColumn.SortType.toggle() = when (this) {
        TableColumn.SortType.ASCENDING -> TableColumn.SortType.DESCENDING
        TableColumn.SortType.DESCENDING -> TableColumn.SortType.ASCENDING
    }
}
