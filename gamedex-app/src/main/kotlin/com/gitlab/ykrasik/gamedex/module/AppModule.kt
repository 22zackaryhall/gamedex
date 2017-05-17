package com.gitlab.ykrasik.gamedex.module

import com.gitlab.ykrasik.gamedex.core.*
import com.gitlab.ykrasik.gamedex.repository.GameRepository
import com.gitlab.ykrasik.gamedex.repository.LibraryRepository
import com.gitlab.ykrasik.gamedex.settings.*
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
    fun newDirectoryDetector() = newDirectoryDetector

    @Provides
    @Singleton
    fun allSettings(general: GeneralSettings,
                    provider: ProviderSettings,
                    game: GameSettings,
                    gameWall: GameWallSettings) =
        AllSettings(general, provider, game, gameWall)

    @Provides
    @Singleton
    fun generalSettings() = GeneralSettings()

    @Provides
    @Singleton
    fun providerSettings() = ProviderSettings()

    @Provides
    @Singleton
    fun gameSettings() = GameSettings()

    @Provides
    @Singleton
    fun gameWallSettings() = GameWallSettings()
}