package com.gitlab.ykrasik.gamedex.ui.view.game.search

import com.gitlab.ykrasik.gamedex.core.SearchChooser
import com.gitlab.ykrasik.gamedex.ui.theme.*
import com.gitlab.ykrasik.gamedex.ui.verticalSeparator
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.Screen
import tornadofx.*
import java.util.*

/**
 * User: ykrasik
 * Date: 02/01/2017
 * Time: 15:54
 */
class SearchResultsFragment(data: SearchChooser.Data) : Fragment("Choose Search Result for '${data.name}'") {
    private val results = ArrayList(data.results).observable()

    private val header = SearchResultsHeaderFragment(data) { close(it) }
    private val content = SearchResultsContentFragment(results) { close(it) }

    private var choice: SearchChooser.Choice = SearchChooser.Choice.Cancel

    override val root = borderpane {
        with(Screen.getPrimary().bounds) {
            prefHeight = height * 3 / 4
            prefWidth = width * 2 / 3
        }
        center {
            borderpane {
                paddingAll = 20
                top = header.root.apply {
                    paddingBottom = 10
                }
                center = content.root.apply {
                    vgrow = Priority.ALWAYS
                }
            }
        }
        top {
            toolbar {
                acceptButton {
                    enableWhen { content.root.selectionModel.selectedItemProperty().isNotNull }
                    setOnAction { close(SearchChooser.Choice.ExactMatch(content.root.selectedItem!!)) }
                }
                verticalSeparator()
                toolbarButton("Not Exact Match", Theme.Icon.question()) {
                    setId(Style.notExactMatch)
                    tooltip("Not Exact Match")
                    enableWhen { content.root.selectionModel.selectedItemProperty().isNotNull }
                    setOnAction { close(SearchChooser.Choice.NotExactMatch(content.root.selectedItem!!)) }
                }
                verticalSeparator()
                backButton("Proceed Without") {
                    setId(Style.proceedWithout)
                    tooltip("Proceed Without")
                    setOnAction { close(SearchChooser.Choice.ProceedWithout) }
                }
                verticalSeparator()
                spacer()
                verticalSeparator()
                excludeButton("Exclude ${data.providerId}") {
                    tooltip("Exclude searching ${data.providerId} for '${data.name}'")
                    setOnAction { close(SearchChooser.Choice.ExcludeProvider(data.providerId)) }
                }
                verticalSeparator()
                cancelButton { setOnAction { close(SearchChooser.Choice.Cancel) } }
            }
        }
    }

    init {
        header.showingFilteredProperty.onChange {
            if (it) results += data.filteredResults else results -= data.filteredResults
            content.root.resizeColumnsToFitContent()
        }
        if (results.isEmpty() && data.filteredResults.isNotEmpty()) {
            header.showingFiltered = true
        }
    }

    override fun onDock() {
//        SmartResize.POLICY.requestResize(tableView)   // TODO: Experiment with this.
        content.root.resizeColumnsToFitContent()
        modalStage!!.minWidthProperty().bind(content.minTableWidth.add(60))
    }

    fun show(): SearchChooser.Choice {
        openWindow(block = true)
        return choice
    }

    private fun close(choice: SearchChooser.Choice) {
        this.choice = choice
        close()
    }

    class Style : Stylesheet() {
        companion object {
            val notExactMatch by cssid()
            val proceedWithout by cssid()

            init {
                importStylesheet(Style::class)
            }
        }

        init {
            notExactMatch {
                and(hover) {
                    backgroundColor = multi(Color.LIGHTYELLOW)
                }
            }
            proceedWithout {
                and(hover) {
                    backgroundColor = multi(Color.YELLOW)
                }
            }
        }
    }
}