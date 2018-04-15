/****************************************************************************
 * Copyright (C) 2016-2018 Yevgeny Krasik                                   *
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

package com.gitlab.ykrasik.gamedex.provider.giantbomb

import com.gitlab.ykrasik.gamedex.*
import com.gitlab.ykrasik.gamedex.provider.ProviderSearchResult
import com.gitlab.ykrasik.gamedex.test.ScopedWordSpec
import com.typesafe.config.ConfigFactory
import io.kotlintest.matchers.shouldBe

/**
 * User: ykrasik
 * Date: 08/10/2016
 * Time: 17:59
 */
class GiantBombProviderContractTest : ScopedWordSpec() {
    val config = GiantBombConfig(ConfigFactory.load("com/gitlab/ykrasik/gamedex/provider/giantbomb/giantbomb.conf"))
    val provider = GiantBombProvider(config, GiantBombClient(config))
    val account = GiantBombUserAccount(apiKey = System.getProperty("gameDex.giantBomb.apiKey"))

    init {
        "GiantBombProvider" should {
            "search & retrieve a single search result" {
                provider.search(name, Platform.pc, account) shouldBe listOf(
                    ProviderSearchResult(
                        apiUrl = apiUrl,
                        name = name,
                        releaseDate = releaseDate,
                        criticScore = null,
                        userScore = null,
                        thumbnailUrl = thumbnailUrl
                    )
                )
            }

            "download game details" {
                provider.download(apiUrl, Platform.pc, account) shouldBe ProviderData(
                    header = ProviderHeader(
                        id = "GiantBomb",
                        apiUrl = apiUrl,
                        updateDate = nowMock
                    ),
                    gameData = GameData(
                        siteUrl = url,
                        name = name,
                        description = deck,
                        releaseDate = releaseDate,
                        criticScore = null,
                        userScore = null,
                        genres = genres,
                        imageUrls = ImageUrls(
                            thumbnailUrl = thumbnailUrl,
                            posterUrl = posterUrl,
                            screenshotUrls = screenshotUrls
                        )
                    )
                )
            }
        }
    }

    val name = "No Man's Sky"
    val apiUrl = "https://www.giantbomb.com/api/game/3030-44656/"
    val releaseDate = "2016-08-09"
    val thumbnailUrl = "https://www.giantbomb.com/api/image/scale_avatar/2927125-no%20man%27s%20sky.jpg"
    val posterUrl = "https://www.giantbomb.com/api/image/scale_large/2927125-no%20man%27s%20sky.jpg"
    val url = "https://www.giantbomb.com/no-mans-sky/3030-44656/"
    val deck = "A procedurally generated space exploration game from Hello Games, the creators of Joe Danger."
    val genres = listOf("Simulation", "Action-Adventure")
    val screenshotUrls = listOf(
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958078-2017-08-19_1304_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958077-2017-08-19_1237_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958076-2017-08-16_2318_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958075-2017-08-13_1537_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958074-2017-08-13_1530_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958073-2017-08-13_1335_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958072-2017-08-13_0151_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958071-2017-08-13_0011_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958070-2017-08-12_1626_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/6/66643/2958069-2017-08-19_1325_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/3699/2927125-no+man%27s+sky.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888664-20160918132648_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888663-20160918132159_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888662-20160918131545_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888661-20160918131538_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888660-20160918130507_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888659-20160918125007_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888658-20160918124920_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888657-20160918124730_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888656-20160918124710_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888655-20160918123455_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888654-20160918122956_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888653-20160918121440_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888652-20160918120459_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888651-20160918120359_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888650-20160918120229_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888649-20160918120220_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888648-20160918120210_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888647-20160918120147_1.jpg",
        "https://static.giantbomb.com/uploads/scale_large/0/5684/2888646-20160918120022_1.jpg"
    )
}