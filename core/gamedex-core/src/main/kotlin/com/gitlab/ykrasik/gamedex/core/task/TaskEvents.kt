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

package com.gitlab.ykrasik.gamedex.core.task

import com.gitlab.ykrasik.gamedex.app.api.util.Task
import com.gitlab.ykrasik.gamedex.core.CoreEvent
import kotlinx.coroutines.experimental.Deferred

/**
 * User: ykrasik
 * Date: 02/11/2018
 * Time: 14:54
 */
data class TaskStartedEvent<T>(val task: Task<T>) : CoreEvent
data class TaskFinishedEvent<T>(val task: Task<T>, val result: Deferred<T>) : CoreEvent