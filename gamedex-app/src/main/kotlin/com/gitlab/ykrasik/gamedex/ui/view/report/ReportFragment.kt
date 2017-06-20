package com.gitlab.ykrasik.gamedex.ui.view.report

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.controller.GameController
import com.gitlab.ykrasik.gamedex.controller.ReportsController
import com.gitlab.ykrasik.gamedex.core.ReportConfig
import com.gitlab.ykrasik.gamedex.core.ReportRule
import com.gitlab.ykrasik.gamedex.core.RuleResult
import com.gitlab.ykrasik.gamedex.core.matchesSearchQuery
import com.gitlab.ykrasik.gamedex.repository.GameProviderRepository
import com.gitlab.ykrasik.gamedex.ui.*
import com.gitlab.ykrasik.gamedex.ui.theme.CommonStyle
import com.gitlab.ykrasik.gamedex.ui.theme.Theme
import com.gitlab.ykrasik.gamedex.ui.theme.pathButton
import com.gitlab.ykrasik.gamedex.ui.theme.toDisplayString
import com.gitlab.ykrasik.gamedex.ui.view.game.menu.GameContextMenu
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import tornadofx.*

/**
 * User: ykrasik
 * Date: 11/06/2017
 * Time: 09:48
 */
// TODO: Should consider making this a view and just re-binding the report to it.
class ReportFragment(val reportConfig: ReportConfig) : View(reportConfig.name, Theme.Icon.book()) {
    private val gameContextMenu: GameContextMenu by inject()

    private val reportsController: ReportsController by di()
    private val gameController: GameController by di()
    private val providerRepository: GameProviderRepository by di()

    private var gamesTable: TableView<Game> by singleAssign()

    val report = reportsController.generateReport(reportConfig)

    val searchProperty = SimpleStringProperty("")

    override val root = run {
        val games = report.resultsProperty.mapToList { it.keys.sortedBy { it.name } }
        val left = container(games.mapProperty { "Games: ${it.size}" }) {
            gamesTable = gamesView(games)
            gamesTable
        }

        val right = container(selectedGameProperty.map { "Violations: ${report.results[it]?.size ?: 0}" }) {
            violationsView()
        }
        splitpane(left, right) { setDividerPositions(0.45) }
    }

    private fun EventTarget.gamesView(games: ObservableList<Game>) = tableview(games) {
        vgrow = Priority.ALWAYS
        fun isNotSelected(game: Game) = selectionModel.selectedItemProperty().isNotEqualTo(game)

        makeIndexColumn().apply { addClass(CommonStyle.centered) }
        column("Game", Game::name)
        customGraphicColumn("Path") { game ->
            pathButton(game.path) { mouseTransparentWhen { isNotSelected(game) } }
        }

        gameContextMenu.install(this) { selectionModel.selectedItem }
        onUserSelect { gameController.viewDetails(it) }

        searchProperty.onChange { query ->
            if (query.isNullOrEmpty()) return@onChange
            val match = items.filter { it.matchesSearchQuery(query!!) }.firstOrNull()
            if (match != null) {
                selectionModel.select(match)
                scrollTo(match)
            }
        }

        report.resultsProperty.onChange { resizeColumnsToFitContent() }
    }

    private fun EventTarget.violationsView() = tableview<RuleResult.Fail> {
        vgrow = Priority.ALWAYS

        // FIXME: Render each violation in a per-report way.
        makeIndexColumn().apply { addClass(CommonStyle.centered) }
        simpleColumn("Rule") { violation -> violation.rule }
        customGraphicColumn("Value") { violation ->
            when (violation.value) {
                is ReportRule.Rules.GameNameFolderDiff ->
                    // TODO: Move all of this into the diff renderer and rename it to diff fragment.
                    Form().apply {
                        val diff = violation.value
                        addClass(CommonStyle.centered)
                        fieldset {
                            field {
                                imageview(providerRepository[diff.providerId].logoImage) {
                                    fitHeight = 80.0
                                    fitWidth = 160.0
                                    isPreserveRatio = true
                                }
                            }
                            field("Expected") { children += DiffRenderer.renderExpected(diff) }
                            field("Actual") { children += DiffRenderer.renderActual(diff) }
                        }
                    }
                else -> label(violation.value.toDisplayString())
            }
        }

        selectedGameProperty.onChange { selectedGame ->
            items = selectedGame?.let { report.results[it]!!.observable() }
            resizeColumnsToFitContent()
        }
    }

    private val selectedGameProperty get() = gamesTable.selectionModel.selectedItemProperty()
    private val selectedGame get() = gamesTable.selectionModel.selectedItem
    private fun selectGame(game: Game) = gamesTable.selectionModel.select(game)
    private fun scrollTo(game: Game) = gamesTable.scrollTo(game)

    private fun container(text: ObservableValue<String>, op: VBox.() -> Unit) = vbox {
        alignment = Pos.CENTER
        label(text) { addClass(Style.headerLabel) }
        op(this)
    }

    override fun onDock() = report.start()
    override fun onUndock() = report.stop()

    class Style : Stylesheet() {
        companion object {
            val headerLabel by cssclass()

            init {
                importStylesheet(Style::class)
            }
        }

        init {
            headerLabel {
                fontSize = 18.px
                fontWeight = FontWeight.BOLD
            }
        }
    }
}