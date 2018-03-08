package com.gitlab.ykrasik.gamedex.provider.giantbomb

import com.gitlab.ykrasik.gamedex.*
import com.gitlab.ykrasik.gamedex.test.*
import io.kotlintest.matchers.*
import io.kotlintest.mock.`when`
import io.kotlintest.mock.mock

/**
 * User: ykrasik
 * Date: 19/04/2017
 * Time: 09:21
 */
class GiantBombProviderTest : ScopedWordSpec() {
    init {
        "GiantBombProvider.search" should {
            "be able to return a single search result".inScope(Scope()) {
                val searchResult = searchResult(name = name)

                givenClientSearchReturns(listOf(searchResult), name = name)

                search(name) shouldBe listOf(ProviderSearchResult(
                    apiUrl = searchResult.apiDetailUrl,
                    name = name,
                    releaseDate = searchResult.originalReleaseDate?.toString(),
                    criticScore = null,
                    userScore = null,
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

                search() should haveASingleSearchResultThat {
                    it.criticScore shouldBe null
                }
            }

            "handle null image".inScope(Scope()) {
                givenClientSearchReturns(listOf(searchResult().copy(image = null)))

                search() should haveASingleSearchResultThat {
                    it.thumbnailUrl shouldBe null
                }
            }

            "consider GiantBomb logo url as absent image".inScope(Scope()) {
                givenClientSearchReturns(listOf(searchResult().copy(image = randomImage().copy(thumbUrl = "${randomUrl()}/2853576-gblogo.png"))))

                search() should haveASingleSearchResultThat {
                    it.thumbnailUrl shouldBe null
                }
            }

            "throw GameDexException on invalid response status".inScope(Scope()) {
                givenClientSearchReturns(GiantBombClient.SearchResponse(GiantBombClient.Status.badFormat, emptyList()))

                shouldThrow<GamedexException> {
                    search()
                }
            }
        }

        "GiantBombProvider.download" should {
            "download details".inScope(Scope()) {
                val detailsResult = detailsResult()

                givenClientFetchReturns(detailsResult, apiUrl = apiDetailUrl)

                download(apiDetailUrl) shouldBe ProviderData(
                    header = ProviderHeader(
                        id = "GiantBomb",
                        apiUrl = apiDetailUrl,
                        updateDate = nowMock
                    ),
                    gameData = GameData(
                        siteUrl = detailsResult.siteDetailUrl,
                        name = detailsResult.name,
                        description = detailsResult.deck,
                        releaseDate = detailsResult.originalReleaseDate?.toString(),
                        criticScore = null,
                        userScore = null,
                        genres = listOf(detailsResult.genres!!.first().name)
                    ),
                    imageUrls = ImageUrls(
                        thumbnailUrl = detailsResult.image!!.thumbUrl,
                        posterUrl = detailsResult.image!!.superUrl,
                        screenshotUrls = detailsResult.images.map { it.superUrl }
                    )
                )
            }

            "handle null deck".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(deck = null))

                download().gameData.description shouldBe null
            }

            "handle null originalReleaseDate".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(originalReleaseDate = null))

                download().gameData.releaseDate shouldBe null
            }

            "handle null genres".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(genres = null))

                download().gameData.genres shouldBe emptyList<String>()
            }

            "handle null image".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(image = null))

                download().imageUrls.thumbnailUrl shouldBe null
                download().imageUrls.posterUrl shouldBe null
            }

            "consider GiantBomb logo url as absent thumbnail".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(image = randomImage().copy(thumbUrl = "${randomUrl()}/2853576-gblogo.png")))

                download().imageUrls.thumbnailUrl shouldBe null
            }

            "consider GiantBomb logo url as absent poster".inScope(Scope()) {
                givenClientFetchReturns(detailsResult().copy(image = randomImage().copy(superUrl = "${randomUrl()}/2853576-gblogo.png")))

                download().imageUrls.posterUrl shouldBe null
            }

            "filter out GiantBomb logo url from screenshots".inScope(Scope()) {
                val screenshot1 = randomImage()
                givenClientFetchReturns(detailsResult().copy(images = listOf(randomImage().copy(superUrl = "${randomUrl()}/2853576-gblogo.png"), screenshot1)))

                download().imageUrls.screenshotUrls shouldBe listOf(screenshot1.superUrl)
            }

            "throw GameDexException on invalid response status".inScope(Scope()) {
                givenClientFetchReturns(GiantBombClient.DetailsResponse(GiantBombClient.Status.badFormat, emptyList()))

                shouldThrow<GamedexException> {
                    download()
                }
            }
        }
    }

    class Scope {
        val platform = randomEnum<Platform>()
        val name = randomName()
        val apiDetailUrl = randomUrl()
        val account = GiantBombUserAccount(apiKey = randomString())

        fun randomImage() = GiantBombClient.Image(thumbUrl = randomUrl(), superUrl = randomUrl())

        fun searchResult(name: String = this.name) = GiantBombClient.SearchResult(
            apiDetailUrl = randomUrl(),
            name = name,
            originalReleaseDate = randomLocalDate(),
            image = randomImage()
        )

        fun detailsResult(name: String = this.name) = GiantBombClient.DetailsResult(
            siteDetailUrl = randomUrl(),
            name = name,
            deck = randomSentence(),
            originalReleaseDate = randomLocalDate(),
            image = randomImage(),
            images = listOf(randomImage(), randomImage()),
            genres = listOf(GiantBombClient.Genre(randomString()))
        )

        fun givenClientSearchReturns(results: List<GiantBombClient.SearchResult>, name: String = this.name) =
            givenClientSearchReturns(GiantBombClient.SearchResponse(GiantBombClient.Status.ok, results), name)

        fun givenClientSearchReturns(response: GiantBombClient.SearchResponse, name: String = this.name) {
            `when`(client.search(name, platform, account)).thenReturn(response)
        }

        fun givenClientFetchReturns(result: GiantBombClient.DetailsResult, apiUrl: String = apiDetailUrl) =
            givenClientFetchReturns(GiantBombClient.DetailsResponse(GiantBombClient.Status.ok, listOf(result)), apiUrl)

        fun givenClientFetchReturns(response: GiantBombClient.DetailsResponse, apiUrl: String = apiDetailUrl) {
            `when`(client.fetch(apiUrl, account)).thenReturn(response)
        }

        fun search(name: String = this.name) = provider.search(name, platform, account)

        fun download(apiUrl: String = apiDetailUrl, platform: Platform = this.platform) = provider.download(apiUrl, platform, account)

        private val config = GiantBombConfig("", "", "", ProviderOrderPriorities.default, emptyMap())
        private val client = mock<GiantBombClient>()
        val provider = GiantBombProvider(config, client)

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