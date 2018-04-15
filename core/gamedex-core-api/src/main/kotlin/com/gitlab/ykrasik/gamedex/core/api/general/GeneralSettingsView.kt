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

package com.gitlab.ykrasik.gamedex.core.api.general

import com.gitlab.ykrasik.gamedex.core.api.util.BroadcastReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel

/**
 * User: ykrasik
 * Date: 02/04/2018
 * Time: 09:27
 */
interface GeneralSettingsView {
    val canRunTask: SendChannel<Boolean>

    val exportDatabaseEvents: BroadcastReceiveChannel<Unit>
    val importDatabaseEvents: BroadcastReceiveChannel<Unit>

    val clearUserDataEvents: BroadcastReceiveChannel<Unit>
    val cleanupDbEvents: BroadcastReceiveChannel<Unit>
}