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

package com.gitlab.ykrasik.gamedex.core.provider

import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.app.api.util.MultiChannel
import com.gitlab.ykrasik.gamedex.app.api.util.MultiReceiveChannel
import com.gitlab.ykrasik.gamedex.core.filter.FilterService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 31/12/2018
 * Time: 15:12
 */
@Singleton
class BulkSyncGamesFilterRepository @Inject constructor(
    private val filterService: FilterService
) {
    private val _bulkSyncGamesFilter = MultiChannel.conflated(Filter.Null)
    val bulkSyncGamesFilter: MultiReceiveChannel<Filter> = _bulkSyncGamesFilter.distinctUntilChanged(Filter::isEqual)

    init {
        // Init default filter.
        _bulkSyncGamesFilter.offer(filterService.getOrPutSystemFilter(filterName) { Filter.Null })
    }

    fun update(filter: Filter) {
        filterService.putSystemFilter(filterName, filter)
        _bulkSyncGamesFilter.offer(filter)
    }

    private companion object {
        val filterName = BulkSyncGamesFilterRepository::class.qualifiedName!!
    }
}