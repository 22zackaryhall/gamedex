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

package com.gitlab.ykrasik.gamedex.core.filter

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter
import com.gitlab.ykrasik.gamedex.core.file.FileSystemService
import com.google.inject.ImplementedBy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 03/10/2018
 * Time: 22:53
 */
@ImplementedBy(FilterContextFactoryImpl::class)
interface FilterContextFactory {
    fun create(games: List<Game>): Filter.Context
}

@Singleton
class FilterContextFactoryImpl @Inject constructor(
    private val fileSystemService: FileSystemService
) : FilterContextFactory{
    override fun create(games: List<Game>): Filter.Context = FilterContextImpl(games, fileSystemService)
}