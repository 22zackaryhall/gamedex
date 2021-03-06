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

package com.gitlab.ykrasik.gamedex

import org.joda.time.DateTime

/**
 * User: ykrasik
 * Date: 09/02/2019
 * Time: 15:31
 */
data class Version(
    val version: String,
    val buildDate: DateTime?,
    val commitHash: String?,
    val commitDate: DateTime?
) {
    companion object {
        val Null = Version(
            version = "",
            buildDate = null,
            commitHash = null,
            commitDate = null
        )
    }
}