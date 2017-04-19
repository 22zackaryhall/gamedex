package com.gitlab.ykrasik.gamedex.provider.giantbomb

import com.gitlab.ykrasik.gamedex.common.exception.GameDexException
import com.gitlab.ykrasik.gamedex.common.testkit.*
import com.gitlab.ykrasik.gamedex.datamodel.*
import com.gitlab.ykrasik.gamedex.provider.ProviderFetchResult
import com.gitlab.ykrasik.gamedex.provider.ProviderSearchResult
import io.kotlintest.matchers.Matcher
import io.kotlintest.matchers.Matchers
import io.kotlintest.mock.`when`
import io.kotlintest.mock.mock

/**
 * User: ykrasik
 * Date: 19/04/2017
 * Time: 09:21
 */
class GiantBombDataProviderTest : ScopedWordSpec() {
    init {
        "GiantBombDataProvider.search" should {
            "be able to return a single search result".inScope(Scope()) {
                val searchResult = searchResult(name = name)

                givenClientSearchReturns(listOf(searchResult), name = name)

                search(name) shouldBe listOf(ProviderSearchResult(
                    apiUrl = searchResult.apiDetailUrl,
                    name = name,
                    releaseDate = searchResult.originalReleaseDate,
                    score = null,
                    thumbnailUrl = searchResult.image!!.thumbUrl
                ))
            }

            "be able to return empty search results".inScope(Scope()) {
                givenClientSearchReturns(emptyList())

                search() shouldBe emptyList<ProviderSearchResult>()
            }

            "be able to return multiple search results".inScope(Scope()) {
                val searchResult1 = searchResult(name)
                val searchResult2 = searchResult("$name ${randomString()}")
                givenClientSearchReturns(listOf(searchResult1, searchResult2), name)

                search(name) should have2SearchResultsThat { first, second ->
                    first.name shouldBe searchResult1.name
                    second.name shouldBe searchResult2.name
                }
            }

            "handle null originalReleaseDate".inScope(Scope()) {
                givenClientSearchReturns(listOf(searchResult().copy(originalReleaseDate = null)))

                search() should haveSearchResultThat {
                    it.score shouldBe null
                }
            }

            "handle null image".inScope(Scope()) {
                givenClientSearchReturns(listOf(searchResult().copy(image = null)))

                search() should haveSearchResultThat {
                    it.thumbnailUrl shouldBe null
                }
            }

            "throw GameDexException on invalid response status".inScope(Scope()) {
                givenClientSearchReturns(GiantBomb.SearchResponse(GiantBomb.Status.badFormat, emptyList()))

                shouldThrow<GameDexException> {
                    search()
                }
            }
        }

        "GiantBombDataProvider.fetch" should {
            "fetch a search result".inScope(Scope()) {
                val detailsResult = detailsResult()

                givenClientFetchReturns(detailsResult, apiUrl = apiDetailUrl)

                fetch(apiDetailUrl) shouldBe ProviderFetchResult(
                    providerData = ProviderData(
                        type = DataProviderType.GiantBomb,
                        apiUrl = apiDetailUrl,
                        url = detailsResult.siteDetailUrl
                    ),
                    gameData = GameData(
                        name = detailsResult.name,
                        description = detailsResult.description,
                        releaseDate = detailsResult.originalReleaseDate,
                        criticScore = null,
                        userScore = null,
                        genres = listOf(detailsResult.genres!!.first().name)
                    ),
                    imageUrls = ImageUrls(
                        thumbnailUrl = detailsResult.image!!.thumbUrl,
                        posterUrl = detailsResult.image!!.superUrl,
                        screenshotUrls = emptyList()
                    )
                )
            }

            "handle null description".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(description = null))

                fetch().gameData.description shouldBe null
            }

            "handle null originalReleaseDate".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(originalReleaseDate = null))

                fetch().gameData.releaseDate shouldBe null
            }

            "handle null genres".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(genres = null))

                fetch().gameData.genres shouldBe emptyList<String>()
            }

            "handle null image".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(image = null))

                fetch().imageUrls.thumbnailUrl shouldBe null
                fetch().imageUrls.posterUrl shouldBe null
            }

            "throw GameDexException on invalid response status".inScope(Scope()) {
                givenClientFetchReturns(GiantBomb.DetailsResponse(GiantBomb.Status.badFormat, emptyList()))

                shouldThrow<GameDexException> {
                    fetch()
                }
            }
        }
    }

    class Scope : Matchers {
        val platform = randomEnum<GamePlatform>()
        val platformId = rnd.nextInt(100)
        val name = randomName()
        val apiDetailUrl = randomUrl()

        fun searchResult(name: String = this.name) = GiantBomb.SearchResult(
            apiDetailUrl = randomUrl(),
            name = name,
            originalReleaseDate = randomLocalDate(),
            image = GiantBomb.SearchImage(thumbUrl = randomUrl())
        )

        fun detailsResult(name: String = this.name) = GiantBomb.DetailsResult(
            siteDetailUrl = randomUrl(),
            name = name,
            description = randomSentence(),
            originalReleaseDate = randomLocalDate(),
            image = GiantBomb.DetailsImage(thumbUrl = randomUrl(), superUrl = randomUrl()),
            genres = listOf(GiantBomb.Genre(randomString()))
        )

        fun givenClientSearchReturns(results: List<GiantBomb.SearchResult>, name: String = this.name) =
            givenClientSearchReturns(GiantBomb.SearchResponse(GiantBomb.Status.ok, results), name)

        fun givenClientSearchReturns(response: GiantBomb.SearchResponse, name: String = this.name) {
            `when`(client.search(name, platform)).thenReturn(response)
        }

        fun givenClientFetchReturns(result: GiantBomb.DetailsResult, apiUrl: String = apiDetailUrl) =
            givenClientFetchReturns(GiantBomb.DetailsResponse(GiantBomb.Status.ok, listOf(result)), apiUrl)

        fun givenClientFetchReturns(response: GiantBomb.DetailsResponse, apiUrl: String = apiDetailUrl) {
            `when`(client.fetch(apiUrl)).thenReturn(response)
        }

        fun search(name: String = this.name) = provider.search(name, platform)

        fun fetch(apiUrl: String = apiDetailUrl, platform: GamePlatform = this.platform) = provider.fetch(apiUrl, platform)

        private val client = mock<GiantBombClient>()
        val provider = GiantBombDataProvider(client)

        fun haveSearchResultThat(f: (ProviderSearchResult) -> Unit) = object : Matcher<List<ProviderSearchResult>> {
            override fun test(value: List<ProviderSearchResult>) {
                value should haveSize(1)
                f(value.first())
            }
        }

        fun have2SearchResultsThat(f: (first: ProviderSearchResult, second: ProviderSearchResult) -> Unit) = object : Matcher<List<ProviderSearchResult>> {
            override fun test(value: List<ProviderSearchResult>) {
                value should haveSize(2)
                f(value[0], value[1])
            }
        }
    }
}