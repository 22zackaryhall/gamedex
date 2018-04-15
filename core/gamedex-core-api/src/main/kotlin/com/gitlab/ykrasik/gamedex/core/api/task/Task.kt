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

package com.gitlab.ykrasik.gamedex.core.api.task

import com.gitlab.ykrasik.gamedex.core.api.util.conflatedChannel
import com.gitlab.ykrasik.gamedex.core.api.util.getValue
import com.gitlab.ykrasik.gamedex.core.api.util.setValue
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.coroutineContext

/**
 * User: ykrasik
 * Date: 31/03/2018
 * Time: 19:35
 */
// TODO: Split into QuickTask, LongTask & LongCancellableTask
interface ReadOnlyTask<out T> {
    val type: TaskType
    val title: String

    val processed: Int
    val totalWork: Int?

    val message1Channel: ReceiveChannel<String>
    val message2Channel: ReceiveChannel<String>
    val progressChannel: ReceiveChannel<Double>

    val doneMessage: Deferred<String?>

    val subTasks: ReceiveChannel<ReadOnlyTask<*>> // TODO: ListObservable?

    suspend fun run(): T  // TODO: Return a deferred?
}

class Task<out T>(override val title: String,
                  override val type: TaskType = TaskType.Long,
                  private val errorHandler: (Exception) -> Unit = ::defaultErrorHandler,
                  private val doRun: suspend Task<T>.() -> T) : ReadOnlyTask<T> {
    private var started = false
    private lateinit var context: CoroutineContext

    private val _processed = AtomicInteger(0)
    override val processed: Int get() = _processed.get()

    override var totalWork: Int? = null
        set(value) {
            field = value
            _processed.set(0)
            progress = 0.0
        }

    private val _message1Channel = conflatedChannel<String>()
    var message1 by _message1Channel
    override val message1Channel: ReceiveChannel<String> = _message1Channel

    private val _message2Channel = conflatedChannel<String>()
    var message2 by _message2Channel
    override val message2Channel: ReceiveChannel<String> = _message2Channel

    private val _progressChannel = conflatedChannel<Double>()
    var progress by _progressChannel
    override val progressChannel: ReceiveChannel<Double> = _progressChannel

    private var doneMessageSupplier: ((Boolean) -> String)? = null
    override val doneMessage = CompletableDeferred<String?>()

    // TODO: Differentiate between error termination & cancel?
    fun doneMessageOrCancelled(message: String) = doneMessage { success -> if (success) message else "Cancelled" }

    fun doneMessage(msg: (success: Boolean) -> String) {
        doneMessageSupplier = msg
    }

    override val subTasks = Channel<Task<*>>()

    fun progress(done: Int, total: Int) {
        progress = done.toDouble() / total.toDouble()
    }

    fun incProgress(amount: Int = 1) = progress(_processed.addAndGet(amount), totalWork!!)

//    // FIXME: Expose this as a builder-style api
//    suspend fun <R> step(message: String, doRun: suspend Task<*>.() -> R): R {
//        this.message1 = message
//        return runSubTask {
//            doRun()
//        }.incProgress()
//    }

//    fun <T> List<T>.reportProgress() = reportProgress(this@Task)

    override suspend fun run(): T = run(CommonPool)

    suspend fun <R> runSubTask(type: TaskType = TaskType.Long,
                               errorHandler: (Exception) -> Unit = this.errorHandler,
                               doRun: suspend Task<*>.() -> R) =
        runSubTask(Task("", type, errorHandler, doRun))

    suspend fun <R> runSubTask(subTask: Task<R>): R {
        subTasks.send(subTask)
        return subTask.run(context)
    }

    private suspend fun run(context: CoroutineContext): T {
        require(!started) { "Task was already run: $this" }

        var success = false
        return try {
            started = true
            async(context) {
                doRun()
            }.apply {
                this@Task.context = coroutineContext
            }.await().apply {
                success = true
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                errorHandler(e)
            }
            throw e
        } finally {
            doneMessage.complete(doneMessageSupplier?.invoke(success))
            close()
        }
    }

    private fun close() {
        _message1Channel.close()
        _message2Channel.close()
        _progressChannel.close()
        subTasks.close()
    }

    inline fun <T, R> List<T>.mapWithProgress(task: Task<*> = this@Task, f: (T) -> R): List<R> = task.run {
        totalWork = size
        map { f(it).apply { incProgress() } }
    }

    inline fun <T, R : Any> List<T>.mapNotNullWithProgress(task: Task<*> = this@Task, f: (T) -> R?): List<R> = task.run {
        totalWork = size
        mapNotNull { f(it).apply { incProgress() } }
    }

    inline fun <T, R> List<T>.flatMapWithProgress(task: Task<*> = this@Task, f: (T) -> List<R>): List<R> = task.run {
        totalWork = size
        flatMap { f(it).apply { incProgress() } }
    }

    inline fun <T> List<T>.filterWithProgress(task: Task<*> = this@Task, f: (T) -> Boolean): List<T> = task.run {
        totalWork = size
        filter { f(it).apply { incProgress() } }
    }

    inline fun <T> List<T>.forEachWithProgress(task: Task<*> = this@Task, f: (T) -> Unit) = task.run {
        totalWork = size
        forEach { f(it).apply { incProgress() } }
    }

    companion object {
        fun defaultErrorHandler(e: Exception) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
        }
    }
}

enum class TaskType { Quick, Long, NonCancellable }

//class ProgressList<out T>(val wrapped: List<T>, val task: Task<*>) : List<T> by wrapped {
//    inline fun <R> map(f: (T) -> R): List<R> = task.run {
//        totalWork = size
//        wrapped.map { f(it).incProgress() }
//    }
//
//    inline fun <R : Any> mapNotNull(f: (T) -> R?): List<R> = task.run {
//        totalWork = size
//        wrapped.mapNotNull { f(it).incProgress() }
//    }
//
//    inline fun <R> flatMap(f: (T) -> List<R>): List<R> = task.run {
//        totalWork = size
//        wrapped.flatMap { f(it).incProgress() }
//    }
//
//    inline fun filter(f: (T) -> Boolean): List<T> = task.run {
//        totalWork = size
//        wrapped.filter { f(it).incProgress() }
//    }
//
//    fun forEach(f: (T) -> Unit) = task.run {
//        totalWork = size
//        wrapped.forEach { f(it).incProgress() }
//    }
//}
//
//fun <T> List<T>.reportProgress(task: Task<*>) = ProgressList(this, task)