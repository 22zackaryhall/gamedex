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

package com.gitlab.ykrasik.gamedex.core.module

import com.gitlab.ykrasik.gamedex.core.api.file.FileSystemService
import com.gitlab.ykrasik.gamedex.core.api.game.GamePresenter
import com.gitlab.ykrasik.gamedex.core.api.game.GameRepository
import com.gitlab.ykrasik.gamedex.core.api.general.GeneralSettingsPresenter
import com.gitlab.ykrasik.gamedex.core.api.image.ImageRepository
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryRepository
import com.gitlab.ykrasik.gamedex.core.api.provider.GameProviderRepository
import com.gitlab.ykrasik.gamedex.core.api.provider.GameProviderService
import com.gitlab.ykrasik.gamedex.core.file.FileSystemServiceImpl
import com.gitlab.ykrasik.gamedex.core.file.NewDirectoryDetector
import com.gitlab.ykrasik.gamedex.core.game.GameConfig
import com.gitlab.ykrasik.gamedex.core.game.GamePresenterImpl
import com.gitlab.ykrasik.gamedex.core.game.GameRepositoryImpl
import com.gitlab.ykrasik.gamedex.core.game.GameSettings
import com.gitlab.ykrasik.gamedex.core.general.GeneralSettings
import com.gitlab.ykrasik.gamedex.core.general.GeneralSettingsPresenterImpl
import com.gitlab.ykrasik.gamedex.core.image.ImageConfig
import com.gitlab.ykrasik.gamedex.core.image.ImageRepositoryImpl
import com.gitlab.ykrasik.gamedex.core.library.LibraryRepositoryImpl
import com.gitlab.ykrasik.gamedex.core.provider.GameProviderRepositoryImpl
import com.gitlab.ykrasik.gamedex.core.provider.GameProviderServiceImpl
import com.gitlab.ykrasik.gamedex.core.provider.ProviderSettings
import com.gitlab.ykrasik.gamedex.core.report.ReportSettings
import com.gitlab.ykrasik.gamedex.core.settings.AllSettings
import com.gitlab.ykrasik.gamedex.core.settings.UserSettings
import com.gitlab.ykrasik.gamedex.core.util.ClassPathScanner
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.multibindings.Multibinder
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 27/03/2018
 * Time: 21:55
 */
object CoreModule : AbstractModule() {
    override fun configure() {
        bind(FileSystemService::class.java).to(FileSystemServiceImpl::class.java)
        bind(LibraryRepository::class.java).to(LibraryRepositoryImpl::class.java)
        bind(GameRepository::class.java).to(GameRepositoryImpl::class.java)
        bind(GameProviderRepository::class.java).to(GameProviderRepositoryImpl::class.java)
        bind(ImageRepository::class.java).to(ImageRepositoryImpl::class.java)

        bind(GameProviderService::class.java).to(GameProviderServiceImpl::class.java)

        bind(GamePresenter::class.java).to(GamePresenterImpl::class.java)
        bind(GeneralSettingsPresenter::class.java).to(GeneralSettingsPresenterImpl::class.java)

        with(Multibinder.newSetBinder(binder(), UserSettings::class.java)) {
            addBinding().to(GameSettings::class.java)
            addBinding().to(GeneralSettings::class.java)
            addBinding().to(ProviderSettings::class.java)
            addBinding().to(ReportSettings::class.java)
        }
        bind(AllSettings::class.java)
    }

    @Provides
    @Singleton
    fun config(): Config {
        val configurationFiles = ClassPathScanner.scanResources("com.gitlab.ykrasik.gamedex") {
            it.endsWith(".conf") && it != "application.conf" && it != "reference.conf"
        }

        // Use the default config as a baseline and apply 'withFallback' on it for every custom .conf file encountered.
        return configurationFiles.fold(ConfigFactory.load()) { current, url ->
            current.withFallback(ConfigFactory.parseURL(url))
        }
    }

    @Provides
    @Singleton
    fun newDirectoryDetector(config: Config) =
        Class.forName(config.getString("gameDex.newDirectoryDetector.class")).newInstance() as NewDirectoryDetector

    @Provides
    @Singleton
    fun gameConfig(config: Config): GameConfig = config.extract("gameDex.game")

    @Provides
    @Singleton
    fun imageConfig(config: Config): ImageConfig = config.extract("gameDex.image")
}