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

package com.gitlab.ykrasik.gamedex.app.api.game

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.util.BroadcastReceiveChannel

/**
 * User: ykrasik
 * Date: 06/05/2018
 * Time: 18:10
 */
interface TagGameView {
    var game: Game

    val tags: MutableList<String>
    val checkedTags: MutableSet<String>

    var toggleAll: Boolean
    val checkAllChanges: BroadcastReceiveChannel<Boolean>

    val checkTagChanges: BroadcastReceiveChannel<Pair<String, Boolean>>

    var newTagName: String
    val newTagNameChanges: BroadcastReceiveChannel<String>

    var nameValidationError: String?

    val addNewTagActions: BroadcastReceiveChannel<Unit>

    val acceptActions: BroadcastReceiveChannel<Unit>
    val cancelActions: BroadcastReceiveChannel<Unit>
}