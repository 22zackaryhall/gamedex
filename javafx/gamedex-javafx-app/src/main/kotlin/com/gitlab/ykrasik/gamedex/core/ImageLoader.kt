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

package com.gitlab.ykrasik.gamedex.core

import com.gitlab.ykrasik.gamedex.core.persistence.PersistenceService
import com.gitlab.ykrasik.gamedex.javafx.Theme.Images
import com.gitlab.ykrasik.gamedex.javafx.toImage
import com.gitlab.ykrasik.gamedex.util.download
import com.gitlab.ykrasik.gamedex.util.logger
import com.google.inject.Inject
import com.google.inject.Singleton
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * User: ykrasik
 * Date: 02/01/2017
 * Time: 18:30
 */
@Singleton
open class ImageLoader @Inject constructor(private val persistenceService: PersistenceService) {
    private val log = logger()

    private val notAvailable = SimpleObjectProperty(Images.notAvailable)

    private val downloadCache = Cache<String, ByteArray>(300)
    private val downloadImageCache = Cache<String, ReadOnlyObjectProperty<Image>>(300)
    private val fetchImageCache = Cache<String, ReadOnlyObjectProperty<Image>>(5000)

    // Will only ever be called by the JavaFx thread, so we don't need to handle cache concurrency
    // We disregard the value of persistIfAbsent when checking the cache, which is a simplifying assumption -
    // the same urls will always have the same persistIfAbsent value - a situation where this method is called
    // twice - once with persistIfAbsent=false, and again with the same url but persistIfAbsent=true simply doesn't exist.
    open fun fetchImage(gameId: Int, url: String?, persistIfAbsent: Boolean): ReadOnlyObjectProperty<Image> {
        if (url == null) return notAvailable
        return fetchImageCache.getOrPut(url) {
            loadImage {
                fetchOrDownloadImage(gameId, url, persistIfAbsent)
            }
        }
    }

    // Will only ever be called by the JavaFx thread, so we don't need to handle cache concurrency
    open fun downloadImage(url: String?): ReadOnlyObjectProperty<Image> {
        if (url == null) return notAvailable
        return downloadImageCache.getOrPut(url) {
            loadImage {
                downloadImage(url)
            }
        }
    }

    private fun loadImage(f: suspend () -> ByteArray): ObjectProperty<Image> {
        val imageProperty = SimpleObjectProperty(Images.loading)
        launch(CommonPool) {
            val image = f().toImage()
            withContext(JavaFx) {
                imageProperty.value = image
            }
        }
        return imageProperty
    }

    private fun fetchOrDownloadImage(gameId: Int, url: String, persistIfAbsent: Boolean): ByteArray {
        val storedImage = persistenceService.fetchImage(url)
        if (storedImage != null) return storedImage

        val downloadedImage = downloadImage(url)
        if (persistIfAbsent) {
            launch(CommonPool) {
                // Save downloaded image asynchronously
                persistenceService.insertImage(gameId, url, downloadedImage)
            }
        }
        return downloadedImage
    }

    private fun downloadImage(url: String): ByteArray = downloadCache.getOrPut(url) {
        log.trace("[$url] Downloading...")
        val bytes = download(url)
        log.trace("[$url] Downloading... Done: ${bytes.size} bytes.")
        return bytes
    }

    private class Cache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize, 1f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }
}