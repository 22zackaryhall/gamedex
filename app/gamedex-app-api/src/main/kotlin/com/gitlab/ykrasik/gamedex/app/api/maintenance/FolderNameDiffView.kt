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

package com.gitlab.ykrasik.gamedex.app.api.maintenance

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.State
import com.gitlab.ykrasik.gamedex.app.api.UserMutableState
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import difflib.Patch
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * User: ykrasik
 * Date: 27/04/2019
 * Time: 13:08
 */
interface FolderNameDiffView {
    val diffs: MutableList<FolderNameDiffs>

    val searchText: UserMutableState<String>
    val matchingGame: State<Game?>

//    val excludeGameActions: ReceiveChannel<Game>

    val hideViewActions: ReceiveChannel<Unit>
}

data class FolderNameDiffs(
    val game: Game,
    val diffs: List<FolderNameDiff>
)

data class FolderNameDiff(
    val providerId: ProviderId,
    val folderName: String,
    val expectedFolderName: String,
    val patch: Patch<Char>
)