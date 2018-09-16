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

package com.gitlab.ykrasik.gamedex.core.settings.presenter

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.app.api.settings.ProviderAccountState
import com.gitlab.ykrasik.gamedex.app.api.settings.ProviderSettingsView
import com.gitlab.ykrasik.gamedex.core.Presentation
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.api.provider.GameProviderService
import com.gitlab.ykrasik.gamedex.core.settings.ProviderSettingsRepository
import com.gitlab.ykrasik.gamedex.core.settings.SettingsService
import com.gitlab.ykrasik.gamedex.util.browseToUrl
import com.gitlab.ykrasik.gamedex.util.logger
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 20/06/2018
 * Time: 19:47
 */
@Singleton
class ProviderSettingsPresenter @Inject constructor(
    private val settingsService: SettingsService,
    private val gameProviderService: GameProviderService
) : Presenter<ProviderSettingsView> {
    private val log = logger()

    override fun present(view: ProviderSettingsView) = object : Presentation() {
        init {
            view.providerLogos = gameProviderService.logos

            // FIXME: This doesn't update when settings are reset to default.
            view.currentAccount = settingsService.provider.providers[view.provider.id]!!.account
            view.enabled = settingsService.provider.providers[view.provider.id]!!.enabled
            view.lastVerifiedAccount = if (view.currentAccount.isNotEmpty()) view.currentAccount else emptyMap()
            view.state = when {
                view.provider.accountFeature == null -> ProviderAccountState.NotRequired
                accountHasEmptyFields() -> ProviderAccountState.Empty
                view.currentAccount.isNotEmpty() -> ProviderAccountState.Valid
                else -> ProviderAccountState.Empty
            }
            view.isCheckingAccount = false

            // FIXME: This doesn't update when settings are updated outside of this scope, like the settings screen being closed with a cancel.
            view.enabledChanges.forEach { onEnabledChanged(it) }
            view.accountUrlClicks.forEach { onAccountUrlClicked() }
            view.currentAccountChanges.forEach { onCurrentAccountChanged(it) }
            view.verifyAccountRequests.forEach { verifyAccount() }
        }

        private fun accountHasEmptyFields(): Boolean =
            view.currentAccount.size != view.provider.accountFeature!!.fields ||
                view.currentAccount.any { (_, value) -> value.isBlank() }

        private suspend fun onEnabledChanged(enabled: Boolean) {
            if (enabled) {
                if (view.state == ProviderAccountState.Unverified) {
                    verifyAccount()
                }
                if (view.state == ProviderAccountState.Valid || view.state == ProviderAccountState.NotRequired) {
                    modifyProviderSettings { copy(enabled = true, account = view.currentAccount) }
                } else {
                    view.enabled = false
                    view.onInvalidAccount()
                }
            } else {
                modifyProviderSettings { copy(enabled = false) }
            }
        }

        private inline fun modifyProviderSettings(crossinline f: ProviderSettingsRepository.ProviderSettings.() -> ProviderSettingsRepository.ProviderSettings) {
            settingsService.provider.modify { modifyProvider(view.provider.id, f) }
        }

        private suspend fun verifyAccount() {
            val provider = view.provider
            if (provider.accountFeature == null) return

            view.isCheckingAccount = true
            try {
                val newAccount = provider.accountFeature!!.createAccount(view.currentAccount)
                log.info("[${provider.id}] Validating: $newAccount")
                withContext(Dispatchers.IO) {
                    provider.search("TestSearchToVerifyAccount", Platform.pc, newAccount)
                }
                log.info("[${provider.id}] Valid!")
                view.state = ProviderAccountState.Valid
                view.lastVerifiedAccount = view.currentAccount
            } catch (e: Exception) {
                log.warn("[${provider.id}] Invalid!", e)
                view.state = ProviderAccountState.Invalid
            } finally {
                view.isCheckingAccount = false
            }
        }

        private fun onCurrentAccountChanged(currentAccount: Map<String, String>) {
            view.state = when {
                view.provider.accountFeature == null -> ProviderAccountState.NotRequired
                accountHasEmptyFields() -> ProviderAccountState.Empty
                currentAccount == view.lastVerifiedAccount -> ProviderAccountState.Valid
                else -> ProviderAccountState.Unverified
            }
        }

        private fun onAccountUrlClicked() {
            view.provider.accountFeature!!.accountUrl.browseToUrl()
        }
    }
}