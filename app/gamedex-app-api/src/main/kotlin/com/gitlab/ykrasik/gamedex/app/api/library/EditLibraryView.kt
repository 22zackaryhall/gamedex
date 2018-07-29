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

package com.gitlab.ykrasik.gamedex.app.api.library

import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.Platform
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import java.io.File

/**
 * User: ykrasik
 * Date: 21/04/2018
 * Time: 07:05
 */
interface EditLibraryView {
    var library: Library?

    var name: String
    val nameChanges: ReceiveChannel<String>

    var path: String
    val pathChanges: ReceiveChannel<String>

    var platform: Platform
    val platformChanges: ReceiveChannel<Platform>

    var nameValidationError: String?
    var pathValidationError: String?

    val browseActions: ReceiveChannel<Unit>
    val acceptActions: ReceiveChannel<Unit>
    val cancelActions: ReceiveChannel<Unit>

    fun selectDirectory(initialDirectory: File?): File?
}