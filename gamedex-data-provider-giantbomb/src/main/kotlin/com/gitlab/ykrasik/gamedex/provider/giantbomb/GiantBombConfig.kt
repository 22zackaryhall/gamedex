package com.gitlab.ykrasik.gamedex.provider.giantbomb

import com.github.ykrasik.gamedex.common.defaultConfig
import com.github.ykrasik.gamedex.common.getObjectMap
import com.github.ykrasik.gamedex.datamodel.GamePlatform
import com.typesafe.config.Config

/**
 * User: ykrasik
 * Date: 29/05/2016
 * Time: 10:53
 */
data class GiantBombConfig(
    val applicationKey: String,
    private val platforms: Map<GamePlatform, Int>
) {
    fun getPlatformId(platform: GamePlatform) = platforms[platform]!!

    companion object {
        operator fun invoke(config: Config = defaultConfig): GiantBombConfig =
            config.getConfig("gameDex.provider.giantBomb").let { config ->
                GiantBombConfig(
                    applicationKey = config.getString("applicationKey"),
                    platforms = config.getObjectMap("platforms", { GamePlatform.valueOf(it) }, { it as Int })
                )
            }
    }
}