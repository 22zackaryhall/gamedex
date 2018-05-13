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

package com.gitlab.ykrasik.gamedex.core.provider

import com.gitlab.ykrasik.gamedex.app.api.util.ListObservableImpl
import com.gitlab.ykrasik.gamedex.core.api.provider.EnabledGameProvider
import com.gitlab.ykrasik.gamedex.core.api.util.combineLatest
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import com.gitlab.ykrasik.gamedex.provider.GameProvider
import com.gitlab.ykrasik.gamedex.util.info
import com.gitlab.ykrasik.gamedex.util.logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 07/04/2017
 * Time: 21:30
 */
@Singleton
class GameProviderRepository @Inject constructor(providers: MutableSet<GameProvider>, userConfigRepository: UserConfigRepository) {
    private val log = logger()
    private val providerUserConfig = userConfigRepository[ProviderUserConfig::class]

    val allProviders: List<GameProvider> = providers.sortedBy { it.id }
    val enabledProviders = ListObservableImpl<EnabledGameProvider>()

    init {
        providerUserConfig.providerSettingsSubject.combineLatest(providerUserConfig.searchOrderSubject) { providerSettings, searchOrder ->
            providerSettings.mapNotNull { (providerId, settings) ->
                if (!settings.enable) return@mapNotNull null

                val provider = allProviders.find { it.id == providerId }!!
                val account = provider.accountFeature?.createAccount(settings.account!!)
                EnabledGameProvider(provider, account)
            }.sortedWith(searchOrder.toComparator())
        }.subscribe { enabledProviders ->
            this@GameProviderRepository.enabledProviders.setAll(enabledProviders)
        }

        log.info { "Detected providers: $allProviders" }
        log.info { "Enabled providers: ${enabledProviders.sortedBy { it.id }}" }
    }
}