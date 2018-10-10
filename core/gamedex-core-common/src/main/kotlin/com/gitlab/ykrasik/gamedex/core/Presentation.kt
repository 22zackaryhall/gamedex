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

import com.gitlab.ykrasik.gamedex.app.api.util.*
import com.gitlab.ykrasik.gamedex.core.settings.SettingsRepository
import com.gitlab.ykrasik.gamedex.util.setAll
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.reflect.KMutableProperty0

/**
 * User: ykrasik
 * Date: 24/04/2018
 * Time: 08:17
 */
interface Presenter<in V> {
    fun present(view: V): Presentation
}

abstract class Presentation : CoroutineScope {
    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job

    private var _showing = false
    protected val showing get() = _showing

    fun show() {
        check(!_showing) { "Presenter already showing: $this" }
        _showing = true
        onShow()
    }

    protected open fun onShow() {}

    fun hide() {
        check(_showing) { "Presenter wasn't showing: $this" }
        _showing = false
        onHide()
    }

    protected open fun onHide() {}

    fun destroy() {
        if (_showing) hide()
        job.cancel()
    }

    // TODO: This used to be inline, but the kotlin compiler was failing with internal errors. Make inline when solved.
    protected fun <T> ReceiveChannel<T>.forEach(f: suspend (T) -> Unit) {
        launch {
            consumeEach {
                try {
                    f(it)
                } catch (e: Exception) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
                }
            }
        }
    }

    protected inline fun <T> ReceiveChannel<T>.forEachImmediately(crossinline f: (T) -> Unit) {
        f(poll()!!)
        forEach { f(it) }
    }

    // TODO: This used to be inline, but the kotlin compiler was failing with internal errors. Make inline when solved.
    protected fun <T> BroadcastReceiveChannel<T>.forEach(f: suspend (T) -> Unit) = subscribe().forEach(f)

    protected inline fun <T> BroadcastReceiveChannel<T>.forEachImmediately(crossinline f: (T) -> Unit) = subscribe().forEachImmediately(f)

    protected fun <T> ListObservable<T>.bindTo(list: MutableList<T>) {
        list.setAll(this)
        changesChannel.forEach { event ->
            when (event) {
                is ListItemAddedEvent -> list += event.item
                is ListItemsAddedEvent -> list += event.items
                is ListItemRemovedEvent -> list.removeAt(event.index)
                is ListItemsRemovedEvent -> list.removeAll(event.items)
                is ListItemSetEvent -> list[event.index] = event.item
                is ListItemsSetEvent -> list.setAll(event.items)
            }
        }
    }

    protected inline fun <S : SettingsRepository<Data>, T, Data : Any> S.bind(
        channelAccessor: S.() -> BroadcastReceiveChannel<T>,
        viewProperty: KMutableProperty0<T>,
        changesChannel: ReceiveChannel<T>,
        crossinline f: (Data).(T) -> Data
    ) {
        val channel = channelAccessor(this)
        channel.reportChangesTo(viewProperty)
        changesChannel.forEach { change ->
            this.modify { f(change) }
        }
    }

    protected fun <T> BroadcastReceiveChannel<T>.reportChangesTo(viewProperty: KMutableProperty0<T>) {
        viewProperty.set(peek()!!)
        forEach { viewProperty.set(it) }
    }
}