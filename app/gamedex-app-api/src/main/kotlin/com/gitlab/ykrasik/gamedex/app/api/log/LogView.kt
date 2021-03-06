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

package com.gitlab.ykrasik.gamedex.app.api.log

import com.gitlab.ykrasik.gamedex.app.api.util.SettableList
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

/**
 * User: ykrasik
 * Date: 06/05/2018
 * Time: 12:53
 */
interface LogView {
    val entries: SettableList<LogEntry>

    val clearLogActions: Flow<Unit>
}

typealias LogEntryId = Int

data class LogEntry(
    val id: LogEntryId,
    val timestamp: DateTime,
    val level: LogLevel,
    val threadName: String,
    val loggerName: String,
    val message: String,
    val throwable: Throwable?
)

enum class LogLevel(val displayName: String) {
    Trace("Trace"),
    Debug("Debug"),
    Info("Info"),
    Warn("Warn"),
    Error("Error");

    fun canLog(level: LogLevel) = this.ordinal >= level.ordinal
    fun canLog(entry: LogEntry) = entry.level.canLog(this)
}