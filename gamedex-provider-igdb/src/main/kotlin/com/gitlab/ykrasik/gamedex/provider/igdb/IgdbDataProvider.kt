package com.gitlab.ykrasik.gamedex.provider.igdb

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.gitlab.ykrasik.gamedex.common.util.*
import com.gitlab.ykrasik.gamedex.datamodel.*
import com.gitlab.ykrasik.gamedex.provider.DataProvider
import com.gitlab.ykrasik.gamedex.provider.DataProviderInfo
import com.gitlab.ykrasik.gamedex.provider.ProviderFetchResult
import com.gitlab.ykrasik.gamedex.provider.ProviderSearchResult
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 29/01/2017
 * Time: 11:14
 */
@Singleton
class IgdbDataProvider @Inject constructor(
    private val config: IgdbConfig,
    private val resultAdapter: IgdbResultAdapter = IgdbResultAdapter(config)
) : DataProvider {
    private val log by logger()

    private val searchFields = listOf(
        "name",
        "aggregated_rating",
        "release_dates.category",
        "release_dates.human",
        "release_dates.platform",
        "cover.cloudinary_id"
    ).joinToString(",")

    private val fetchDetailsFields = listOf(
        "url",
        "summary",
        "rating",
        "cover.cloudinary_id",
        "screenshots.cloudinary_id",
        "genres"
    ).joinToString(",")

    override fun search(name: String, platform: GamePlatform): List<ProviderSearchResult> {
        log.info { "Searching: name='$name', platform=$platform..." }
        val searchResults = doSearch(name, platform)
        log.debug { "Results: $searchResults" }

        val results = resultAdapter.toSearchResults(searchResults, name, platform)
        log.info { "Done(${results.size}): $results." }
        return results
    }

    private fun doSearch(name: String, platform: GamePlatform): List<Igdb.SearchResult> {
        val response = getRequest(config.endpoint,
            "search" to name,
            "filter[release_dates.platform][eq]" to platform.id.toString(),
            "limit" to config.maxSearchResults.toString(),
            "fields" to searchFields
        )
        return response.listFromJson()
    }

    override fun fetch(searchResult: ProviderSearchResult): ProviderFetchResult {
        log.info { "Fetching: $searchResult..." }
        val fetchResult = doFetch(searchResult)
        log.debug { "Response: $fetchResult" }

        // When result is found - GiantBomb returns a Json object.
        // When result is not found, GiantBomb returns an empty Json array [].
        // So 'results' can contain at most a single value.
        val gameData = resultAdapter.toFetchResult(fetchResult, searchResult)
        log.info { "Done: $gameData." }
        return gameData
    }

    private fun doFetch(searchResult: ProviderSearchResult): Igdb.DetailsResult {
        val response = getRequest(searchResult.apiUrl, "fields" to fetchDetailsFields)
        // IGDB returns a list, even though we're fetching by id :/
        return response.listFromJson<Igdb.DetailsResult> { parseError(it) }.first()
    }

    private fun getRequest(path: String, vararg parameters: Pair<String, String>) = khttp.get(path,
        params = parameters.toMap(),
        headers = mapOf(
            "Accept" to "application/json",
            "X-Mashape-Key" to config.apiKey
        )
    )

    private fun parseError(raw: String): String {
        val errors: List<Igdb.Error> = raw.listFromJson()
        return errors.first().error.first()
    }

    private val GamePlatform.id: Int get() = config.getPlatformId(this)

    override val info = Igdb.info
}

open class IgdbResultAdapter(private val config: IgdbConfig) {
    fun toSearchResults(results: List<Igdb.SearchResult>, name: String, platform: GamePlatform): List<ProviderSearchResult> {
        // IGBD search sucks. It returns way more results then it should.
        // Since I couldn't figure out how to make it not return irrelevant results, I had to filter results myself.
        val searchWords = name.split("[^a-zA-Z\\d']".toRegex())
        return results.asSequence().filter { (_, name) ->
            searchWords.all { word ->
                name.containsIgnoreCase(word)
            }
        }.map { it.toSearchResult(platform) }.toList()
    }

    private fun Igdb.SearchResult.toSearchResult(platform: GamePlatform) = ProviderSearchResult(
        apiUrl = "${config.endpoint}$id",
        name = name,
        releaseDate = findReleaseDate(platform),
        score = aggregatedRating,
        thumbnailUrl = cover?.cloudinaryId?.let { imageUrl(it, IgdbImageType.thumb, x2 = true) }
    )

    private fun Igdb.SearchResult.findReleaseDate(platform: GamePlatform): LocalDate? {
        // IGDB returns all release dates for all platforms, not just the one we searched for.
        val releaseDates = this.releaseDates ?: return null
        val releaseDate = releaseDates.find { it.platform == platform.id } ?: return null
        return releaseDate.toLocalDate()
    }

    fun toFetchResult(fetchResult: Igdb.DetailsResult, searchResult: ProviderSearchResult) = ProviderFetchResult(
        providerData = ProviderData(
            type = DataProviderType.Igdb,
            apiUrl = searchResult.apiUrl,
            url = fetchResult.url
        ),
        gameData = GameData(
            name = searchResult.name,
            description = fetchResult.summary,
            releaseDate = searchResult.releaseDate,
            criticScore = searchResult.score,
            userScore = fetchResult.rating,
            genres = fetchResult.genres?.map { it.genreName } ?: emptyList()
        ),
        imageUrls = ImageUrls(
            thumbnailUrl = searchResult.thumbnailUrl,
            posterUrl = fetchResult.cover?.cloudinaryId?.let { imageUrl(it, IgdbImageType.screenshot_huge) },
            screenshotUrls = emptyList()
            // TODO: Support screenshots
        )
    )

    private fun imageUrl(hash: String, type: IgdbImageType, x2: Boolean = false) =
        "${config.baseImageUrl}/t_$type${if (x2) "_2x" else ""}/$hash.png"

    private enum class IgdbImageType {
        micro, // 35 x 35
        thumb, // 90 x 90
        logo_med, // 284 x 160
        cover_small, // 90 x 128
        cover_big, // 227 x 320
        screenshot_med, // 569 x 320
        screenshot_big, // 889 x 500
        screenshot_huge     // 1280 x 720
    }

    private val GamePlatform.id: Int get() = config.getPlatformId(this)
    private val Int.genreName: String get() = config.getGenreName(this)
}

object Igdb {
    val info = DataProviderInfo(
        name = "IGDB",
        type = DataProviderType.Igdb,
        logo = getResourceAsByteArray("/com/gitlab/ykrasik/gamedex/provider/igdb/igdb.png").toImage()
    )

    data class SearchResult(
        val id: Int,
        val name: String,
        val aggregatedRating: Double?,
        val releaseDates: List<ReleaseDate>?,
        val cover: Image?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ReleaseDate(
        val platform: Int,
        val category: Int,
        val human: String
    ) {
        fun toLocalDate(): LocalDate? {
            val format = when (category) {
                0 -> DateTimeFormat.forPattern("YYYY-MMM-dd")
                1 -> DateTimeFormat.forPattern("YYYY-MMM")
                2 -> DateTimeFormat.forPattern("YYYY")
                3 -> DateTimeFormat.forPattern("YYYY-'Q1'")
                4 -> DateTimeFormat.forPattern("YYYY-'Q2'")
                5 -> DateTimeFormat.forPattern("YYYY-'Q3'")
                6 -> DateTimeFormat.forPattern("YYYY-'Q4'")
                7 -> return null
                else -> throw IllegalArgumentException("Invalid date category: $category!")
            }
            return format.parseLocalDate(human)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DetailsResult(
        val url: String,
        val summary: String?,
        val rating: Double?,
        val cover: Image?,
        val screenshots: List<Image>?,
        val genres: List<Int>?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Image(
        val cloudinaryId: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Error(
        val error: List<String>
    )
}