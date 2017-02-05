package com.gitlab.ykrasik.gamedex.provider.igdb

import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.ykrasik.gamedex.common.datamodel.*
import com.github.ykrasik.gamedex.common.util.containsIgnoreCase
import com.github.ykrasik.gamedex.common.util.getResourceAsByteArray
import com.github.ykrasik.gamedex.common.util.logger
import com.github.ykrasik.gamedex.common.util.toImage
import com.gitlab.ykrasik.gamedex.provider.*
import com.gitlab.ykrasik.gamedex.provider.util.listFromJson
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 29/01/2017
 * Time: 11:14
 */
@Singleton
class IgdbDataProvider @Inject constructor(private val config: IgdbConfig) : DataProvider {
    private val log by logger()

    override val info = DataProviderInfo(
        name = "IGDB",
        type = DataProviderType.GiantBomb,
        logo = getResourceAsByteArray("/com/gitlab/ykrasik/gamedex/provider/igdb/igdb.png").toImage()
    )

    private val endpoint = "https://igdbcom-internet-game-database-v1.p.mashape.com/games/"
    private val baseImageUrl = "http://images.igdb.com/igdb/image/upload"

    private val searchFields = listOf(
        "name",
        "aggregated_rating",
        "release_dates.category",
        "release_dates.human",
        "release_dates.platform",
        "cover.cloudinary_id"
    ).joinToString(",")

    private val fetchDetailsFields = listOf(
        "summary",
        "aggregated_rating",
        "rating",
        "cover.cloudinary_id",
        "screenshots.cloudinary_id",
        "genres"
    ).joinToString(",")

    override fun search(name: String, platform: GamePlatform): List<ProviderSearchResult> {
        log.info { "Searching: name='$name', platform=$platform..." }
        val response = doSearch(name, platform)
        log.debug { "Response: $response" }

        val results = toSearchResults(response, name, platform)
        log.info { "Done(${results.size}): $results." }
        return results
    }

    private fun doSearch(name: String, platform: GamePlatform): List<IgdbSearchResult> {
        val (request, response, result) = getRequest(endpoint,
            "search" to name,
            "filter[release_dates.platform][eq]" to platform.id,
            "limit" to 20,
            "fields" to searchFields
        )
        response.assertOk()
        return result.listFromJson()
    }

    private fun IgdbSearchResult.toSearchResult(platform: GamePlatform) = ProviderSearchResult(
        detailUrl = "$endpoint$id",
        name = name,
        releaseDate = findReleaseDate(platform),
        score = aggregatedRating,
        thumbnailUrl = cover?.cloudinaryId?.let { imageUrl(it, IgdbImageType.thumb) }
    )

    private fun IgdbSearchResult.findReleaseDate(platform: GamePlatform): LocalDate? {
        // IGDB returns all release dates for all platforms, not just the one we searched for.
        val releaseDates = this.releaseDates ?: return null
        val releaseDate = releaseDates.find { it.platform == platform.id } ?: return null
        return releaseDate.toLocalDate()
    }

    // TODO: Unit test this.
    private fun toSearchResults(response: List<IgdbSearchResult>, name: String, platform: GamePlatform): List<ProviderSearchResult> {
        // IGBD search sucks. It returns way more results then it should.
        // Since I couldn't figure out how to make it not return irrelevant results, I had to filter results myself.
        val searchWords = name.split("[^a-zA-Z\\d']".toRegex())
        return response.asSequence().filter { result ->
            searchWords.all { word ->
                result.name.containsIgnoreCase(word)
            }
        }.map { it.toSearchResult(platform) }.toList()
    }

    override fun fetch(searchResult: ProviderSearchResult): ProviderFetchResult {
        log.info { "Fetching: $searchResult..." }
        val response = doFetch(searchResult)
        log.debug { "Response: $response" }

        // When result is found - GiantBomb returns a Json object.
        // When result is not found, GiantBomb returns an empty Json array [].
        // So 'results' can contain at most a single value.
        val gameData = response.toFetchResult(searchResult)
        log.info { "Done: $gameData." }
        return gameData
    }

    private fun doFetch(searchResult: ProviderSearchResult): IgdbDetailsResult {
        val (request, response, result) = getRequest(searchResult.detailUrl, "fields" to fetchDetailsFields)
        response.assertOk()
        // IGDB returns a list, even though we're fetching by id :/
        return result.listFromJson<IgdbDetailsResult>().first()
    }

    private fun IgdbDetailsResult.toFetchResult(searchResult: ProviderSearchResult) = ProviderFetchResult(
        providerData = GameProviderData(
            type = DataProviderType.Igdb,
            detailUrl = searchResult.detailUrl
        ),
        gameData = GameData(
            name = searchResult.name,
            description = summary,
            releaseDate = searchResult.releaseDate,
            criticScore = aggregatedRating,
            userScore = rating,
            genres = genres?.map { it.genreName } ?: emptyList()
        ),
        imageData = GameImageData(
            thumbnailUrl = searchResult.thumbnailUrl,
            posterUrl = cover?.cloudinaryId?.let { imageUrl(it, IgdbImageType.screenshot_huge) },
            screenshot1Url = null,
            screenshot2Url = null,
            screenshot3Url = null,
            screenshot4Url = null,
            screenshot5Url = null,
            screenshot6Url = null,
            screenshot7Url = null,
            screenshot8Url = null,
            screenshot9Url = null,
            screenshot10Url = null
            // TODO: Support screenshots
        )
    )

    private fun getRequest(path: String, vararg parameters: Pair<String, Any?>) = path.httpGet(parameters.toList())
        .header("Accept" to "application/json")
        .header("X-Mashape-Key" to config.apiKey)
        .response()

    private fun Response.assertOk() {
        if (httpStatusCode != 200) throw DataProviderException(httpResponseMessage)
    }

    private fun imageUrl(hash: String, type: IgdbImageType, x2: Boolean = false) =
        "$baseImageUrl/t_$type${if (x2) "_2x" else ""}/$hash.png"

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