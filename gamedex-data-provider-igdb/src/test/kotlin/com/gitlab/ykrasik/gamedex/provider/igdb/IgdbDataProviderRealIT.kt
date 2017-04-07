package com.gitlab.ykrasik.gamedex.provider.igdb

import com.gitlab.ykrasik.gamedex.common.datamodel.*
import com.gitlab.ykrasik.gamedex.common.testkit.ScopedWordSpec
import com.gitlab.ykrasik.gamedex.provider.ProviderFetchResult
import com.gitlab.ykrasik.gamedex.provider.ProviderSearchResult
import org.joda.time.LocalDate

/**
 * User: ykrasik
 * Date: 04/12/2017
 * Time: 10:03
 */
class IgdbDataProviderRealIT : ScopedWordSpec() {
    val provider = IgdbDataProvider(IgdbConfig())

    class Scope {
        val name = "No Man's Sky"
        val apiUrl = "https://igdbcom-internet-game-database-v1.p.mashape.com/games/3225"
        val releaseDate = LocalDate.parse("2016-08-12")
        val criticScore = 72.5555555555556
        val userScore = 62.8707346675691
        val thumbnailUrl = "http://images.igdb.com/igdb/image/upload/t_thumb_2x/sixpdbypwojsyly22a1l.png"
        val posterUrl = "http://images.igdb.com/igdb/image/upload/t_screenshot_huge/sixpdbypwojsyly22a1l.png"
        val url = "https://www.igdb.com/games/no-man-s-sky"
        val description = "Inspired by the adventure and imagination that we love from classic science-fiction, No Man's Sky presents you with a galaxy to explore, filled with unique planets and lifeforms, and constant danger and action. \n\nIn No Man's Sky, every star is the light of a distant sun, each orbited by planets filled with life, and you can go to any of them you choose. Fly smoothly from deep space to planetary surfaces, with no loading screens, and no limits. In this infinite procedurally generated universe, you'll discover places and creatures that no other players have seen before - and perhaps never will again."
        val genres = listOf("Shooter", "Role-playing (RPG)", "Simulator", "Adventure", "Indie")

        val searchResult = ProviderSearchResult(
            apiUrl = apiUrl,
            name = name,
            releaseDate = releaseDate,
            score = criticScore,
            thumbnailUrl = thumbnailUrl
        )
    }

    init {
        "IgdbDataProvider" should {
            "Search & retrieve a single search result".inScope(Scope()) {
                val response = provider.search(name, GamePlatform.pc)
                response shouldBe listOf(searchResult)
            }

            "Fetch game details".inScope(Scope()) {
                val response = provider.fetch(searchResult)
                response shouldBe ProviderFetchResult(
                    providerData = ProviderData(
                        type = DataProviderType.Igdb,
                        apiUrl = apiUrl,
                        url = url
                    ),
                    gameData = GameData(
                        name = name,
                        description = description,
                        releaseDate = releaseDate,
                        criticScore = criticScore,
                        userScore = userScore,
                        genres = genres
                    ),
                    imageUrls = ImageUrls(
                        thumbnailUrl = thumbnailUrl,
                        posterUrl = posterUrl,
                        screenshotUrls = emptyList()
                    )
                )
            }
        }
    }
}