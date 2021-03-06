/****************************************************************************
 * Copyright (C) 2016-2020 Yevgeny Krasik                                   *
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

package com.gitlab.ykrasik.gamedex.app.api.provider

import com.gitlab.ykrasik.gamedex.app.api.util.SettableList
import com.gitlab.ykrasik.gamedex.app.api.util.ViewMutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * User: ykrasik
 * Date: 16/10/2018
 * Time: 09:47
 */
interface SyncGamesView {
    val cancelActions: Flow<Unit>

    val isAllowSmartChooseResults: MutableStateFlow<Boolean>

    val isGameSyncRunning: MutableStateFlow<Boolean>
    val numProcessed: MutableStateFlow<Int>

    val state: SettableList<GameSearchState>

    val currentState: ViewMutableStateFlow<GameSearchState?>
    val restartStateActions: Flow<GameSearchState>

    fun successMessage(message: String)
    fun cancelledMessage(message: String)
}
