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

package com.gitlab.ykrasik.gamedex.core.settings

import com.gitlab.ykrasik.gamedex.core.api.util.value_
import io.reactivex.subjects.BehaviorSubject
import kotlin.reflect.KProperty

/**
 * User: ykrasik
 * Date: 11/03/2018
 * Time: 15:03
 */
abstract class UserSettings {
    protected abstract val repo: SettingsRepo<*>

    fun saveSnapshot() = repo.saveSnapshot()
    fun restoreSnapshot() = repo.restoreSnapshot()
    fun clearSnapshot() = repo.clearSnapshot()

    fun disableWrite() = repo.disableWrite()
    fun enableWrite() = repo.enableWrite()

    fun flush() = repo.flush()

    // Convenience shortcuts for subclasses
    operator fun <T> BehaviorSubject<T>.getValue(thisRef: Any, property: KProperty<*>) = value_
    operator fun <T> BehaviorSubject<T>.setValue(thisRef: Any, property: KProperty<*>, value: T) {
        value_ = value
    }
}