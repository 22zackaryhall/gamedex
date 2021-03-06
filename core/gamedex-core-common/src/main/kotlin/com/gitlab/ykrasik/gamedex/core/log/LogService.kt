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

package com.gitlab.ykrasik.gamedex.core.log

import com.gitlab.ykrasik.gamedex.app.api.log.LogEntry
import com.gitlab.ykrasik.gamedex.core.util.ListObservable

/**
 * User: ykrasik
 * Date: 15/06/2019
 * Time: 22:01
 */
interface LogService {
    val entries: ListObservable<LogEntry>

    fun addBlacklistValue(value: String)

    fun clear()
}