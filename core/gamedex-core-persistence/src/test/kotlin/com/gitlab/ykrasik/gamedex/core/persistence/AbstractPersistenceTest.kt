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

package com.gitlab.ykrasik.gamedex.core.persistence

import com.gitlab.ykrasik.gamedex.*
import com.gitlab.ykrasik.gamedex.test.*
import com.gitlab.ykrasik.gamedex.util.toFile
import io.kotlintest.TestCaseContext

/**
 * User: ykrasik
 * Date: 23/04/2017
 * Time: 13:48
 */
abstract class AbstractPersistenceTest : ScopedWordSpec() {
    override fun interceptTestCase(context: TestCaseContext, test: () -> Unit) {
        persistenceService.dropDb()
        test()
    }

    open class LibraryScope {
        fun libraryData(name: String = randomName(),
                        path: String = randomPath(),
                        platform: Platform = randomEnum()) =
            LibraryData(name, path.toFile(), platform)

        fun givenLibrary(path: String = randomPath(),
                         platform: Platform = randomEnum(),
                         name: String = randomName()): Library = insertLibrary(path, platform, name)

        fun insertLibrary(path: String = randomPath(),
                          platform: Platform = randomEnum(),
                          name: String = randomName()): Library =
            persistenceService.insertLibrary(libraryData(name, path, platform))

        fun fetchLibraries() = persistenceService.fetchLibraries()
    }

    open class GameScope : LibraryScope() {
        val library = givenLibrary()

        fun randomMetadata(library: Library = this.library, path: String = randomPath()) = Metadata(
            libraryId = library.id,
            path = path,
            updateDate = randomDateTime()
        )

        fun randomProviderData() = ProviderData(
            header = ProviderHeader(
                id = randomString(),
                apiUrl = randomUrl(),
                updateDate = randomDateTime()
            ),
            gameData = GameData(
                siteUrl = randomUrl(),
                name = randomName(),
                description = randomSentence(),
                releaseDate = randomLocalDateString(),
                criticScore = randomScore(),
                userScore = randomScore(),
                genres = listOf(randomString(), randomString()),
                imageUrls = ImageUrls(
                    thumbnailUrl = randomUrl(),
                    posterUrl = randomUrl(),
                    screenshotUrls = listOf(randomUrl(), randomUrl())
                )
            )
        )

        fun randomUserData() = UserData(
            overrides = randomProviderOverrides(),
            tags = listOf(randomString(), randomString()),
            excludedProviders = listOf(randomString(), randomString())
        )

        fun randomProviderOverrides() = mapOf(
            GameDataType.name_ to providerOverride(),
            GameDataType.description to providerOverride(),
            GameDataType.releaseDate to providerOverride(),
            GameDataType.criticScore to providerOverride(),
            GameDataType.userScore to providerOverride(),
            GameDataType.genres to providerOverride(),
            GameDataType.thumbnail to providerOverride(),
            GameDataType.poster to providerOverride(),
            GameDataType.screenshots to providerOverride()
        )

        fun randomCustomOverrides() = mapOf(
            GameDataType.name_ to customDataOverride(randomName()),
            GameDataType.description to customDataOverride(randomSentence()),
            GameDataType.releaseDate to customDataOverride(randomLocalDateString()),
            GameDataType.criticScore to customDataOverride(randomScore().score),
            GameDataType.userScore to customDataOverride(randomScore().score),
            GameDataType.genres to customDataOverride(listOf(randomString(), randomString(), randomString())),
            GameDataType.thumbnail to customDataOverride(randomUrl()),
            GameDataType.poster to customDataOverride(randomUrl()),
            GameDataType.screenshots to customDataOverride(listOf(randomUrl(), randomUrl()))
        )

        private fun providerOverride() = GameDataOverride.Provider(randomString())
        private fun customDataOverride(data: Any) = GameDataOverride.Custom(data)

        fun givenGame(library: Library = this.library,
                      path: String = randomPath(),
                      providerData: List<ProviderData> = listOf(randomProviderData(), randomProviderData()),
                      userData: UserData? = randomUserData()): RawGame =
            insertGame(library, path, providerData, userData)

        fun insertGame(library: Library = this.library,
                       path: String = randomPath(),
                       providerData: List<ProviderData> = listOf(randomProviderData(), randomProviderData()),
                       userData: UserData? = randomUserData()): RawGame =
            insertGame(randomMetadata(library, path), providerData, userData)

        fun insertGame(metadata: Metadata = randomMetadata(),
                       providerData: List<ProviderData> = listOf(randomProviderData(), randomProviderData()),
                       userData: UserData? = randomUserData()): RawGame =
            persistenceService.insertGame(metadata, providerData, userData)

        fun fetchGames() = persistenceService.fetchGames()
    }

    open class ImageScope : GameScope() {
        val game = givenGame()

        fun givenImage(game: RawGame = this.game, url: String = randomUrl()): List<Byte> = insertImage(game, url)
        fun insertImage(game: RawGame = this.game, url: String = randomUrl()): List<Byte> {
            val data = randomString().toByteArray()
            persistenceService.insertImage(game.id, url, data)
            return data.toList()
        }

        fun fetchImage(url: String) = persistenceService.fetchImage(url)?.toList()
    }

    companion object {
        val persistenceService = PersistenceServiceImpl(PersistenceConfig(
            dbUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
    }
}