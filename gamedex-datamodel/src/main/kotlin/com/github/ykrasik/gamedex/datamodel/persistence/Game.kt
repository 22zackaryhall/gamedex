package com.github.ykrasik.gamedex.datamodel.persistence

import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.nio.file.Path

/**
 * User: ykrasik
 * Date: 26/05/2016
 * Time: 15:32
 */
data class Game(
    val id: Int,
    val path: Path,

    val name: String,
    val description: String?,
    val releaseDate: LocalDate?,

    val criticScore: Double?,
    val userScore: Double?,

    val lastModified: DateTime,

    val metacriticUrl: String,
    val giantBombUrl: String?,

    val genres: List<Genre>,
    val library: Library
) {
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?) = id.equals(other)
}