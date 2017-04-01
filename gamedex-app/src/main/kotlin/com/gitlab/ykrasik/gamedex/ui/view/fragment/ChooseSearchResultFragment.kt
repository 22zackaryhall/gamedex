package com.gitlab.ykrasik.gamedex.ui.view.fragment

import com.gitlab.ykrasik.gamedex.core.ImageLoader
import com.gitlab.ykrasik.gamedex.provider.ChooseSearchResultData
import com.gitlab.ykrasik.gamedex.provider.ProviderSearchResult
import com.gitlab.ykrasik.gamedex.provider.SearchResultChoice
import com.gitlab.ykrasik.gamedex.ui.cancelButton
import com.gitlab.ykrasik.gamedex.ui.customColumn
import com.gitlab.ykrasik.gamedex.ui.fadeOnImageChange
import com.gitlab.ykrasik.gamedex.ui.okButton
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.TableCell
import javafx.scene.control.TableView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.run
import tornadofx.*

/**
 * User: ykrasik
 * Date: 02/01/2017
 * Time: 15:54
 */
class ChooseSearchResultFragment(data: ChooseSearchResultData) : Fragment("Choose Search Result for '${data.name}'") {
    private val imageLoader: ImageLoader by di()
    private val minTableWidth: DoubleProperty = SimpleDoubleProperty()
    private var tableView: TableView<ProviderSearchResult> by singleAssign()

    private var choice: SearchResultChoice = SearchResultChoice.Cancel

    private val thumbnailCache = mutableMapOf<String, ReadOnlyObjectProperty<Image>>()

    override val root = borderpane {
        paddingAll = 20
        top {
            gridpane {
                row {
                    label(data.path.path) {
                        gridpaneConstraints { vAlignment = VPos.TOP; hAlignment = HPos.LEFT }
                        font = Font.font(14.0)
                    }
                    region { gridpaneConstraints { hGrow = Priority.ALWAYS } }
                    imageview {
                        gridpaneConstraints { vAlignment = VPos.TOP; hAlignment = HPos.RIGHT }
                        image = data.info.logo
                        fitHeight = 100.0
                        fitWidth = 100.0
                        isPreserveRatio = true
                    }
                }
                row {
                    gridpane {
                        hgap = 10.0
                        gridpaneConstraints { vAlignment = VPos.BOTTOM; hAlignment = HPos.LEFT }
                        row {
                            val newSearch = textfield(data.name) {
                                prefWidthProperty().bind(minTableWidth.subtract(230))
                                tooltip("You can edit the name and click 'Search Again' to search for a new value")
                                font = Font.font(16.0)
                                isFocusTraversable = false
                            }
                            button("Search Again") {
                                prefHeightProperty().bind(newSearch.heightProperty())
                                tooltip("Search for a new name, enter new name in the name field")
                                setOnAction { close(choice = SearchResultChoice.NewSearch(newSearch.text)) }
                            }
                        }
                    }
                    region { gridpaneConstraints { hGrow = Priority.ALWAYS } }
                    label("Search results: ${data.searchResults.size}") {
                        gridpaneConstraints { vAlignment = VPos.BOTTOM; hAlignment = HPos.RIGHT }
                        font = Font.font(16.0)
                    }
                }
                paddingBottom = 10
            }
        }
        center {
            tableView = tableview(data.searchResults.observable()) {
                val indexColumn = makeIndexColumn()
                customColumn("Thumbnail", ProviderSearchResult::thumbnailUrl) {
                    object : TableCell<ProviderSearchResult, String?>() {
                        private val imageView = ImageView().fadeOnImageChange()
                        init { graphic = imageView }
                        override fun updateItem(thumbnailUrl: String?, empty: Boolean) {
                            if (empty) {
                                imageView.imageProperty().unbind()
                            } else {
                                val thumbnail = thumbnailCache.getOrPut(thumbnailUrl!!) { imageLoader.downloadImage(thumbnailUrl) }
                                imageView.imageProperty().cleanBind(thumbnail)
                            }
                        }
                    }
                }
                column("Name", ProviderSearchResult::name)
                column("Release Date", ProviderSearchResult::releaseDate)
                column("Score", ProviderSearchResult::score)

                minTableWidth.bind(contentColumns.fold(indexColumn.widthProperty().subtract(10)) { binding, column ->
                    binding.add(column.widthProperty())
                })
                minWidthProperty().bind(minTableWidth)

                onUserSelect(clickCount = 2) { close(choice = okResult) }
            }
        }
        bottom {
            buttonbar {
                paddingTop = 20
                okButton { 
                    enableWhen { tableView.selectionModel.selectedItemProperty().isNotNull }
                    setOnAction { close(choice = okResult) }
                }
                cancelButton { setOnAction { close(choice = SearchResultChoice.Cancel) } }
                if (data.canProceedWithout) {
                    button("Proceed Without") { setOnAction { close(choice = SearchResultChoice.ProceedWithout) } }
                }
            }
        }
    }

    override fun onDock() {
        tableView.resizeColumnsToFitContent()
        modalStage!!.minWidthProperty().bind(minTableWidth.add(60))
    }

    suspend fun show(): SearchResultChoice = run(JavaFx) {
        openModal(block = true)
        choice
    }

    private fun close(choice: SearchResultChoice) {
        this.choice = choice
        close()
    }

    private val okResult get() = SearchResultChoice.Ok(tableView.selectedItem!!)
}