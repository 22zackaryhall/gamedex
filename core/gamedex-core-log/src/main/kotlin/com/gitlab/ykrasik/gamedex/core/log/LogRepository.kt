/****************************************************************************
 * Copyright (C) 2016-2019 Yevgeny Krasik                                   *
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
import com.gitlab.ykrasik.gamedex.core.util.ListObservableImpl

/**
 * User: ykrasik
 * Date: 13/04/2018
 * Time: 20:47
 */
class LogRepository(private val maxLogEntries: Int) {
    private val _entries = ListObservableImpl<LogEntry>()
    val entries: ListObservable<LogEntry> = _entries

    operator fun plusAssign(entry: LogEntry) {
        _entries += entry

        if (entries.size > maxLogEntries) {
            _entries -= _entries.subList(0, entries.size - maxLogEntries)
        }
    }
}