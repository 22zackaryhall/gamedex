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

package com.gitlab.ykrasik.gamedex.core.api.task

/**
 * User: ykrasik
 * Date: 05/04/2018
 * Time: 10:55
 */
interface TaskRunner {
    // TODO: This looks like it maybe shouldn't suspend
    suspend fun <T> runTask(task: ReadOnlyTask<T>): T

    suspend fun <T> runTask(title: String,
                            type: TaskType = TaskType.Long,
                            errorHandler: (Exception) -> Unit = Task.Companion::defaultErrorHandler,
                            run: suspend Task<*>.() -> T) =
        runTask(Task(title, type, errorHandler, run))
}