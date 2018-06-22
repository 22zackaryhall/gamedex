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

package com.gitlab.ykrasik.gamedex.app.api.settings

import com.gitlab.ykrasik.gamedex.app.api.image.Image
import com.gitlab.ykrasik.gamedex.provider.GameProvider
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * User: ykrasik
 * Date: 20/06/2018
 * Time: 09:44
 */
interface ProviderSettingsView {
    // TODO: Consider just allowing the view to receive this data through a CommonData object
    var providerLogos: Map<ProviderId, Image>

    var provider: GameProvider

    var state: ProviderAccountState

    var isCheckingAccount: Boolean
    var lastVerifiedAccount: Map<String, String>

    var currentAccount: Map<String, String>
    val currentAccountChanges: ReceiveChannel<Map<String, String>>

    var enabled: Boolean
    val enabledChanges: ReceiveChannel<Boolean>

    val accountUrlClicks: ReceiveChannel<Unit>
    val verifyAccountRequests: ReceiveChannel<Unit>

    fun onInvalidAccount()
}

enum class ProviderAccountState {
    Valid, Invalid, Empty, Unverified, NotRequired
}