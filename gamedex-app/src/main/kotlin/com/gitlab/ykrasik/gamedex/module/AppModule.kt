package com.gitlab.ykrasik.gamedex.module

import com.gitlab.ykrasik.gamedex.core.*
import com.gitlab.ykrasik.gamedex.preferences.*
import com.gitlab.ykrasik.gamedex.repository.GameRepository
import com.gitlab.ykrasik.gamedex.repository.LibraryRepository
import com.google.inject.AbstractModule
import com.google.inject.Provides
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 11/10/2016
 * Time: 11:16
 */
object AppModule : AbstractModule() {
    override fun configure() {
        bind(GameProviderService::class.java).to(GameProviderServiceImpl::class.java)
        bind(SearchChooser::class.java).to(UISearchChooser::class.java)

        // Instruct Guice to eagerly create these classes
        // (during preloading, to avoid the JavaFx thread from lazily creating them on first access)
        bind(GameRepository::class.java)
        bind(LibraryRepository::class.java)
        bind(LibraryScanner::class.java)
        bind(ImageLoader::class.java)
    }

    @Provides
    @Singleton
    fun allPreferences(general: GeneralPreferences,
                       provider: ProviderPreferences,
                       game: GamePreferences,
                       gameWall: GameWallPreferences) =
        AllPreferences(general, provider, game, gameWall)

    @Provides
    @Singleton
    fun generalPreferences() = GeneralPreferences()

    @Provides
    @Singleton
    fun providerPreferences() = ProviderPreferences()

    @Provides
    @Singleton
    fun gamePreferences() = GamePreferences()

    @Provides
    @Singleton
    fun gameWallPreferences() = GameWallPreferences()
}