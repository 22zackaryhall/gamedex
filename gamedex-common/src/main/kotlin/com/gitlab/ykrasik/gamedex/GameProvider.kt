package com.gitlab.ykrasik.gamedex

import org.joda.time.LocalDate

/**
 * User: ykrasik
 * Date: 29/05/2016
 * Time: 10:42
 */
interface GameProvider {
    val name: String
    val type: GameProviderType
    val logo: ByteArray

    fun search(name: String, platform: Platform): List<ProviderSearchResult>

    fun fetch(apiUrl: String, platform: Platform): RawGameData
}

enum class GameProviderType {
    Igdb,
    GiantBomb
}

data class ProviderSearchResult(
    val name: String,
    val releaseDate: LocalDate?,
    val score: Double?,
    val thumbnailUrl: String?,
    val apiUrl: String
)