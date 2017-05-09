package com.gitlab.ykrasik.gamedex.core

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.preferences.GameWallSort
import com.gitlab.ykrasik.gamedex.ui.and
import com.gitlab.ykrasik.gamedex.ui.toPredicate
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import tornadofx.SortedFilteredList
import tornadofx.onChange

/**
 * User: ykrasik
 * Date: 06/05/2017
 * Time: 12:48
 */
class SortedFilteredGames(_games: ObservableList<Game>) {
    val platformFilterProperty = SimpleObjectProperty<Platform>()
    val searchQueryProperty = SimpleStringProperty("")
    val genreFilterProperty = SimpleStringProperty("")

    val sortProperty = SimpleObjectProperty<GameWallSort>(GameWallSort.name_)
    val sortOrderProperty = SimpleObjectProperty<TableColumn.SortType>(TableColumn.SortType.ASCENDING)

    val games: ObservableList<Game> = SortedFilteredList(_games)

    private val nameComparator = Comparator<Game> { o1, o2 -> o1.name.compareTo(o2.name, ignoreCase = true) }
    private val criticScoreComparator = compareBy(Game::criticScore)
    private val userScoreComparator = compareBy(Game::userScore)

    init {
        val platformPredicate = platformFilterProperty.toPredicate { platform, game: Game ->
            game.platform == platform
        }

        val searchPredicate = searchQueryProperty.toPredicate { query, game: Game ->
            query!!.isEmpty() || game.name.contains(query, ignoreCase = true)
        }

        val genrePredicate = genreFilterProperty.toPredicate { genre, game: Game ->
            genre.isNullOrEmpty() || game.genres.contains(genre)
        }

        val gameFilterPredicateProperty = platformPredicate.and(searchPredicate).and(genrePredicate)

        games as SortedFilteredList<Game>
        games.filteredItems.predicateProperty().bind(gameFilterPredicateProperty)
        games.sortedItems.comparatorProperty().bind(sortComparator())
    }

    private fun sortComparator(): ObjectProperty<Comparator<Game>> {
        fun comparator(): Comparator<Game> {
            val comparator = when (sortProperty.value!!) {
                GameWallSort.name_ -> nameComparator
                GameWallSort.criticScore -> criticScoreComparator.then(nameComparator)
                GameWallSort.userScore -> userScoreComparator.then(nameComparator)
                GameWallSort.minScore -> compareBy<Game> { it.minScore }.then(criticScoreComparator).then(userScoreComparator).then(nameComparator)
                GameWallSort.avgScore -> compareBy<Game> { it.avgScore }.then(criticScoreComparator).then(userScoreComparator).then(nameComparator)
                GameWallSort.releaseDate -> compareBy(Game::releaseDate).then(nameComparator)
                GameWallSort.dateAdded -> compareBy(Game::lastModified)
            }
            return if (sortOrderProperty.value == TableColumn.SortType.ASCENDING) {
                comparator
            } else {
                comparator.reversed()
            }
        }

        val property = SimpleObjectProperty(comparator())
        sortOrderProperty.onChange {
            property.value = comparator()
        }
        sortProperty.onChange {
            property.value = comparator()
        }
        return property
    }

    private val Game.minScore get() = criticScore?.let { c -> userScore?.let { u -> minOf(c, u) } }
    private val Game.avgScore get() = criticScore?.let { c -> userScore?.let { u -> (c + u) / 2 } }
}