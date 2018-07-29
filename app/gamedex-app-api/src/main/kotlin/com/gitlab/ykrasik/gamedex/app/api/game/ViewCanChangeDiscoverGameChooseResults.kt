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

import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * User: ykrasik
 * Date: 29/04/2018
 * Time: 14:18
 */
interface ViewCanChangeDiscoverGameChooseResults {
    var discoverGameChooseResults: DiscoverGameChooseResults
    val discoverGameChooseResultsChanges: ReceiveChannel<DiscoverGameChooseResults>
}

enum class DiscoverGameChooseResults(val description: String) {
    chooseIfNonExact("If no exact match: Choose"),
    alwaysChoose("Always choose"),
    skipIfNonExact("If no exact match: Skip"),
    proceedWithoutIfNonExact("If no exact match: Proceed Without")
}