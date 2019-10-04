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

package com.gitlab.ykrasik.gamedex.provider.opencritic

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.provider.GameProvider
import com.typesafe.config.Config
import io.github.config4k.extract

/**
 * User: ykrasik
 * Date: 28/09/2019
 * Time: 22:13
 */
data class OpenCriticConfig(
    val baseUrl: String,
    val defaultOrder: GameProvider.OrderPriorities,
    private val platforms: Map<String, Int>
) {
    private val _platforms = platforms.mapKeys { Platform.valueOf(it.key) }
    fun getPlatformId(platform: Platform) = _platforms.getValue(platform)

    companion object {
        operator fun invoke(config: Config): OpenCriticConfig = config.extract("gameDex.provider.opencritic")
    }
}