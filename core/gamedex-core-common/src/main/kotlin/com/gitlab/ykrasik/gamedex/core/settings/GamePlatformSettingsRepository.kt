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

package com.gitlab.ykrasik.gamedex.core.settings

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.app.api.filter.Filter

/**
 * User: ykrasik
 * Date: 18/06/2018
 * Time: 18:24
 */
class GamePlatformSettingsRepository(factory: SettingsStorageFactory, platform: Platform) : SettingsRepository<GamePlatformSettingsRepository.Data>() {
    data class Data(
        val filter: Filter,
        val search: String
    )

    override val storage = factory(platform.toString().toLowerCase(), Data::class) {
        Data(
            filter = Filter.`true`,
            search = ""
        )
    }

    val filterChannel = storage.channel(Data::filter)
    val filter by filterChannel

    val searchChannel = storage.channel(Data::search)
    val search by searchChannel
}