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

package com.gitlab.ykrasik.gamedex.core.storage

/**
 * User: ykrasik
 * Date: 16/09/2018
 * Time: 14:41
 */
class MemoryCachedStorage<K, V>(private val delegate: Storage<K, V>) : Storage<K, V> {
    private val cache = delegate.getAll().toMutableMap()

    override fun add(value: V): K {
        val key = delegate.add(value)
        cache[key] = value
        return key
    }

    override fun set(key: K, value: V) {
        delegate[key] = value
        cache[key] = value
    }

    override fun setIfNotExists(key: K, value: V): Boolean {
        val success = delegate.setIfNotExists(key, value)
        if (success) {
            cache[key] = value
        }
        return success
    }

    override fun setOnlyIfExists(key: K, value: V) {
        delegate.setOnlyIfExists(key, value)
        cache[key] = value
    }

    override fun setOnlyIfDoesntExist(key: K, value: V) {
        delegate.setOnlyIfDoesntExist(key, value)
        cache[key] = value
    }

    override fun get(key: K): V? {
        val cachedValue = cache[key]
        if (cachedValue != null) return cachedValue

        val value = delegate[key]
        if (value != null) {
            cache[key] = value
        }
        return value
    }

    override fun getAll() = cache

    override fun delete(key: K): Boolean {
        val success = delegate.delete(key)
        cache -= key
        return success
    }

    override fun deleteAll(keys: Iterable<K>) {
        delegate.deleteAll(keys)
        cache -= keys
    }

    override fun deleteOnlyIfExists(key: K) {
        delegate.deleteOnlyIfExists(key)
        cache -= key
    }

    override fun ids() = cache.keys

    override fun sizeTaken(key: K) = delegate.sizeTaken(key)
}

fun <K, V> Storage<K, V>.memoryCached(): Storage<K, V> = MemoryCachedStorage(this)