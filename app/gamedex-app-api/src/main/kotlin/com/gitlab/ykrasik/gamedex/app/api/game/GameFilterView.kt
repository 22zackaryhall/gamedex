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

import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.provider.ProviderId
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.reflect.KClass

/**
 * User: ykrasik
 * Date: 02/06/2018
 * Time: 10:20
 */
interface GameFilterView {
    val possibleGenres: MutableList<String>
    val possibleTags: MutableList<String>
    val possibleLibraries: MutableList<Library>
    val possibleProviderIds: MutableList<ProviderId>
    val possibleRules: MutableList<KClass<out Filter.Rule>>

    var filter: Filter

    val wrapInAndActions: ReceiveChannel<Filter>
    val wrapInOrActions: ReceiveChannel<Filter>
    val wrapInNotActions: ReceiveChannel<Filter>
    val unwrapNotActions: ReceiveChannel<Filter.Not>
    val clearFilterActions: ReceiveChannel<Unit>
    val updateFilterActions: ReceiveChannel<Pair<Filter.Rule, Filter.Rule>>
    val replaceFilterActions: ReceiveChannel<Pair<Filter, KClass<out Filter>>>
    val deleteFilterActions: ReceiveChannel<Filter>
}

interface MenuGameFilterView : GameFilterView
interface ReportGameFilterView : GameFilterView