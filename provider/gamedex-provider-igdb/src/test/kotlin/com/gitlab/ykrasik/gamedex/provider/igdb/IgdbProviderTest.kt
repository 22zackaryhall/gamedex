/****************************************************************************
 * Copyright (C) 2016-2019 Yevgeny Krasik                                   *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package com.gitlab.ykrasik.gamedex.provider.igdb

import com.gitlab.ykrasik.gamedex.GameData
import com.gitlab.ykrasik.gamedex.ImageUrls
import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.Score
import com.gitlab.ykrasik.gamedex.provider.ProviderFetchData
import com.gitlab.ykrasik.gamedex.provider.ProviderOrderPriorities
import com.gitlab.ykrasik.gamedex.provider.ProviderSearchResult
import com.gitlab.ykrasik.gamedex.test.*
import io.kotlintest.matchers.*
import io.mockk.coEvery
import io.mockk.mockk

/**
 * User: ykrasik
 * Date: 16/04/2017
 * Time: 15:41
 */
class IgdbProviderTest : ScopedWordSpec<IgdbProviderTest.Scope>() {
    override fun scope() = Scope()

    init {
        "search" should {
            "be able to return a single search result" test {
                val searchResult = searchResult(name = name, releaseDate = releaseDate)

                givenClientSearchReturns(listOf(searchResult), name = name)

                search(name) shouldBe listOf(
                    ProviderSearchResult(
                        providerGameId = searchResult.id.toString(),
                        name = name,
                        description = searchResult.summary,
                        releaseDate = releaseDate,
                        criticScore = Score(searchResult.aggregatedRating!!, searchResult.aggregatedRatingCount!!),
                        userScore = Score(searchResult.rating!!, searchResult.ratingCount!!),
                        thumbnailUrl = thumbnailUrl(searchResult.cover!!.imageId!!)
                    )
                )
            }

            "be able to return empty search results" test {
                givenClientSearchReturns(emptyList())

                search() shouldBe emptyList<ProviderSearchResult>()
            }

            "be able to return multiple search results" test {
                val searchResult1 = searchResult(name)
                val searchResult2 = searchResult("$name ${randomWord()}")
                givenClientSearchReturns(listOf(searchResult1, searchResult2), name)

                search(name) should have2SearchResultsThat { first, second ->
                    first.name shouldBe searchResult1.name
                    second.name shouldBe searchResult2.name
                }
            }

            "handle null summary" test {
                givenClientSearchReturns(listOf(searchResult().copy(summary = null)))

                search() should haveASingleSearchResultThat {
                    it.description shouldBe null
                }
            }

            "handle null aggregatedRating and ignore aggregatedRatingCount" test {
                givenClientSearchReturns(listOf(searchResult().copy(aggregatedRating = null, aggregatedRatingCount = 1)))

                search() should haveASingleSearchResultThat {
                    it.criticScore shouldBe null
                }
            }

            "handle null rating and ignore ratingCount" test {
                givenClientSearchReturns(listOf(searchResult().copy(rating = null, ratingCount = 1)))

                search() should haveASingleSearchResultThat {
                    it.userScore shouldBe null
                }
            }

            "handle null releaseDates" test {
                givenClientSearchReturns(listOf(searchResult().copy(releaseDates = null)))

                search() should haveASingleSearchResultThat {
                    it.releaseDate shouldBe null
                }
            }

            "use null releaseDate when no releaseDate exists for given platform" test {
                givenClientSearchReturns(listOf(searchResult(releaseDatePlatformId = platformId + 1)))

                search() should haveASingleSearchResultThat {
                    it.releaseDate shouldBe null
                }
            }

            "parse a release date of format YYYY-MMM-dd and return YYYY-MM-dd instead" test {
                givenClientSearchReturns(listOf(searchResult(releaseDate = "2000-Apr-07")))

                search() should haveASingleSearchResultThat {
                    it.releaseDate shouldBe "2000-04-07"
                }
            }

            "fallback to returning the original release date when parsing as YYYY-MMM-dd fails" test {
                givenClientSearchReturns(listOf(searchResult(releaseDate = "2017-Q4")))

                search() should haveASingleSearchResultThat {
                    it.releaseDate shouldBe "2017-Q4"
                }
            }

            "handle null cover cloudinaryId" test {
                givenClientSearchReturns(listOf(searchResult().copy(cover = image(imageId = null))))

                search() should haveASingleSearchResultThat {
                    it.thumbnailUrl shouldBe null
                }
            }

            "handle null cover" test {
                givenClientSearchReturns(listOf(searchResult().copy(cover = null)))

                search() should haveASingleSearchResultThat {
                    it.thumbnailUrl shouldBe null
                }
            }
        }

        "fetch" should {
            "fetch details" test {
                val detailsResult = detailsResult(releaseDate = releaseDate)

                givenClientFetchReturns(detailsResult, providerGameId = baseUrl)

                fetch(baseUrl) shouldBe ProviderFetchData(
                    gameData = GameData(
                        name = detailsResult.name,
                        description = detailsResult.summary,
                        releaseDate = releaseDate,
                        criticScore = Score(detailsResult.aggregatedRating!!, detailsResult.aggregatedRatingCount!!),
                        userScore = Score(detailsResult.rating!!, detailsResult.ratingCount!!),
                        genres = listOf(genre),
                        imageUrls = ImageUrls(
                            thumbnailUrl = thumbnailUrl(detailsResult.cover!!.imageId!!),
                            posterUrl = posterUrl(detailsResult.cover!!.imageId!!),
                            screenshotUrls = detailsResult.screenshots!!.map { screenshotUrl(it.imageId!!) }
                        )
                    ),
                    siteUrl = detailsResult.url
                )
            }

            "handle null summary" test {
                givenClientFetchReturns(detailsResult().copy(summary = null))

                fetch().gameData.description shouldBe null
            }

            "handle null releaseDates" test {
                givenClientFetchReturns(detailsResult().copy(releaseDates = null))

                fetch().gameData.releaseDate shouldBe null
            }

            "use null releaseDate when no releaseDate exists for given platform" test {
                givenClientFetchReturns(detailsResult(releaseDatePlatformId = platformId + 1))

                fetch().gameData.releaseDate shouldBe null
            }

            "parse a release date of format YYYY-MMM-dd and return YYYY-MM-dd instead" test {
                givenClientFetchReturns(detailsResult(releaseDate = "2000-Apr-07"))

                fetch().gameData.releaseDate shouldBe "2000-04-07"
            }

            "fallback to returning the original release date when parsing as YYYY-MMM-dd fails" test {
                givenClientFetchReturns(detailsResult(releaseDate = "2017-Q4"))

                fetch().gameData.releaseDate shouldBe "2017-Q4"
            }

            "handle null aggregatedRating and ignore aggregatedRatingCount" test {
                givenClientFetchReturns(detailsResult().copy(aggregatedRating = null, aggregatedRatingCount = 1))

                fetch().gameData.criticScore shouldBe null
            }

            "handle null rating and ignore ratingCount" test {
                givenClientFetchReturns(detailsResult().copy(rating = null, ratingCount = 1))

                fetch().gameData.userScore shouldBe null
            }

            "handle null genres" test {
                givenClientFetchReturns(detailsResult().copy(genres = null))

                fetch().gameData.genres shouldBe emptyList<String>()
            }

            "handle null cover cloudinaryId" test {
                givenClientFetchReturns(detailsResult().copy(cover = image(imageId = null)))

                fetch().gameData.imageUrls.thumbnailUrl shouldBe null
                fetch().gameData.imageUrls.posterUrl shouldBe null
            }

            "handle null cover" test {
                givenClientFetchReturns(detailsResult().copy(cover = null))

                fetch().gameData.imageUrls.thumbnailUrl shouldBe null
                fetch().gameData.imageUrls.posterUrl shouldBe null
            }

            "handle null screenshot cloudinaryId" test {
                givenClientFetchReturns(detailsResult().copy(screenshots = listOf(image(imageId = null))))

                fetch().gameData.imageUrls.screenshotUrls shouldBe emptyList<String>()
            }

            "handle null & non-null screenshot cloudinaryId" test {
                val image = image()
                givenClientFetchReturns(detailsResult().copy(screenshots = listOf(image(imageId = null), image)))

                fetch().gameData.imageUrls.screenshotUrls shouldBe listOf(screenshotUrl(image.imageId!!))
            }

            "handle null screenshots" test {
                givenClientFetchReturns(detailsResult().copy(screenshots = null))

                fetch().gameData.imageUrls.screenshotUrls shouldBe emptyList<String>()
            }
        }
    }

    class Scope {
        val platform = randomEnum<Platform>()
        val platformId = randomInt(100)
        val genreId = randomInt(100)
        val genre = randomWord()
        val name = randomName()
        val releaseDate = randomLocalDateString()
        val account = IgdbUserAccount(apiKey = randomWord())

        val baseUrl = randomUrl()
        val baseImageUrl = randomUrl()

        fun thumbnailUrl(imageId: String) = "$baseImageUrl/t_thumb_2x/$imageId.jpg"
        fun posterUrl(imageId: String) = "$baseImageUrl/t_screenshot_huge/$imageId.jpg"
        fun screenshotUrl(imageId: String) = posterUrl(imageId)

        fun searchResult(
            name: String = this.name,
            releaseDate: String = randomLocalDateString(),
            releaseDatePlatformId: Int = this.platformId
        ) = IgdbClient.SearchResult(
            id = randomInt(),
            name = name,
            summary = randomParagraph(),
            aggregatedRating = randomScore().score,
            aggregatedRatingCount = randomScore().numReviews,
            rating = randomScore().score,
            ratingCount = randomScore().numReviews,
            releaseDates = listOf(releaseDate(releaseDate, releaseDatePlatformId)),
            cover = image()
        )

        fun detailsResult(
            releaseDate: String = randomLocalDateString(),
            releaseDatePlatformId: Int = this.platformId
        ) = IgdbClient.DetailsResult(
            url = randomWord(),
            name = name,
            summary = randomParagraph(),
            releaseDates = listOf(releaseDate(releaseDate, releaseDatePlatformId)),
            aggregatedRating = randomScore().score,
            aggregatedRatingCount = randomScore().numReviews,
            rating = randomScore().score,
            ratingCount = randomScore().numReviews,
            cover = image(),
            screenshots = listOf(image(), image()),
            genres = listOf(genreId)
        )

        private fun releaseDate(releaseDate: String, platformId: Int) = IgdbClient.ReleaseDate(
            platform = platformId,
            category = 0,
            human = releaseDate
        )

        fun image(imageId: String? = randomString()) = IgdbClient.Image(imageId = imageId)

        fun givenClientSearchReturns(results: List<IgdbClient.SearchResult>, name: String = this.name) {
            coEvery { client.search(name, platform, account) } returns results
        }

        fun givenClientFetchReturns(result: IgdbClient.DetailsResult, providerGameId: String = baseUrl) {
            coEvery { client.fetch(providerGameId, account) } returns result
        }

        suspend fun search(name: String = this.name) = provider.search(name, platform, account)
        suspend fun fetch(providerGameId: String = baseUrl) = provider.fetch(providerGameId, platform, account)

        private val client = mockk<IgdbClient>()
        val provider = IgdbProvider(
            IgdbConfig(
                baseUrl = baseUrl,
                baseImageUrl = baseImageUrl,
                accountUrl = "",
                maxSearchResults = randomInt(),
                thumbnailImageType = IgdbProvider.IgdbImageType.thumb_2x,
                posterImageType = IgdbProvider.IgdbImageType.screenshot_huge,
                screenshotImageType = IgdbProvider.IgdbImageType.screenshot_huge,
                defaultOrder = ProviderOrderPriorities.default,
                platforms = mapOf(platform.name to platformId),
                genres = mapOf(genreId.toString() to genre)
            ), client
        )

        fun haveASingleSearchResultThat(f: (ProviderSearchResult) -> Unit) = object : Matcher<List<ProviderSearchResult>> {
            override fun test(value: List<ProviderSearchResult>): Result {
                value should haveSize(1)
                f(value.first())
                return Result(true, "")
            }
        }

        fun have2SearchResultsThat(f: (first: ProviderSearchResult, second: ProviderSearchResult) -> Unit) = object : Matcher<List<ProviderSearchResult>> {
            override fun test(value: List<ProviderSearchResult>): Result {
                value should haveSize(2)
                f(value[0], value[1])
                return Result(true, "")
            }
        }
    }
}