package com.gitlab.ykrasik.gamedex.ui.view.game.search

import com.gitlab.ykrasik.gamedex.core.SearchChooser
import com.gitlab.ykrasik.gamedex.repository.GameProviderRepository
import com.gitlab.ykrasik.gamedex.ui.imageview
import com.gitlab.ykrasik.gamedex.ui.jfxButton
import com.gitlab.ykrasik.gamedex.ui.map
import com.gitlab.ykrasik.gamedex.ui.theme.Theme
import com.gitlab.ykrasik.gamedex.ui.theme.pathButton
import com.gitlab.ykrasik.gamedex.ui.theme.toLogo
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import tornadofx.*

/**
 * User: ykrasik
 * Date: 09/06/2017
 * Time: 21:31
 */
class SearchResultsHeaderFragment(data: SearchChooser.Data, close: (SearchChooser.Choice) -> Unit) : Fragment() {
    private val providerRepository: GameProviderRepository by di()

    val showingFilteredProperty = SimpleBooleanProperty(false)
    var showingFiltered by showingFilteredProperty

    override val root = gridpane {
        hgap = 10.0

        pathButton(data.path) {
            gridpaneConstraints { columnRowIndex(0, 0); vAlignment = VPos.TOP; hAlignment = HPos.LEFT }
            setId(Style.pathLabel)
        }
        region { gridpaneConstraints { columnRowIndex(1, 0); hGrow = Priority.ALWAYS } }
        stackpane {
            gridpaneConstraints { columnRowIndex(3, 0); columnSpan = 2; vAlignment = VPos.TOP; hAlignment = HPos.RIGHT }
            alignment = Pos.TOP_RIGHT
            children += data.platform.toLogo(46.0)
        }
        imageview(providerRepository.logo(data.providerId)) {
            gridpaneConstraints { columnRowIndex(5, 0); vAlignment = VPos.TOP; hAlignment = HPos.RIGHT }
            fitHeight = 80.0
            fitWidth = 160.0
            isPreserveRatio = true
        }

        val newSearch = textfield(data.name) {
            gridpaneConstraints { columnRowIndex(0, 1) }
            setId(Style.newSearch)
            prefWidth = 3000.0
            tooltip("Search for a different name")
            isFocusTraversable = false
        }
        button("Search Again") {
            gridpaneConstraints { columnRowIndex(1, 1) }
            minWidth = Region.USE_PREF_SIZE
            enableWhen { newSearch.textProperty().isNotEqualTo(data.name) }
            // "Search Again" becomes the new default button when this textfield has focus.
            defaultButtonProperty().bind(newSearch.focusedProperty())
            prefHeightProperty().bind(newSearch.heightProperty())
            tooltip("Search for a different name")
            setOnAction { close(SearchChooser.Choice.NewSearch(newSearch.text)) }
        }
        region { gridpaneConstraints { columnRowIndex(2, 1); hGrow = Priority.ALWAYS } }

        if (data.filteredResults.isNotEmpty()) {
            jfxButton {
                gridpaneConstraints { columnRowIndex(3, 1); vAlignment = VPos.BOTTOM; hAlignment = HPos.RIGHT }
                setId(Style.showFilterToggle)
                graphicProperty().bind(showingFilteredProperty.map {
                    if (it!!) Theme.Icon.minus() else Theme.Icon.plus()
                })
                tooltip {
                    textProperty().bind(showingFilteredProperty.map {
                        "${if (it!!) "Hide" else "Show"} ${data.filteredResults.size} filtered results"
                    })
                }
                setOnAction { showingFiltered = !showingFiltered }
            }
        }
        label {
            gridpaneConstraints { columnRowIndex(5, 1); vAlignment = VPos.BOTTOM; hAlignment = HPos.RIGHT }
            setId(Style.searchResultsLabel)
            minWidth = Region.USE_PREF_SIZE
            textProperty().bind(showingFilteredProperty.map {
                "Search results: ${if (it!!) data.results.size + data.filteredResults.size else data.results.size}"
            })
        }
    }

    class Style : Stylesheet() {
        companion object {
            val pathLabel by cssid()
            val newSearch by cssid()
            val showFilterToggle by cssid()
            val searchResultsLabel by cssid()

            init {
                importStylesheet(Style::class)
            }
        }

        init {
            pathLabel {
                fontSize = 14.px
            }
            newSearch {
                fontSize = 16.px
            }
            showFilterToggle {
                fontSize = 16.px
            }
            searchResultsLabel {
                fontSize = 16.px
            }
        }
    }
}