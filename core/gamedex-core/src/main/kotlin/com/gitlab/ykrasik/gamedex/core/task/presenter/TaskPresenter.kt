/****************************************************************************
 * Copyright (C) 2016-2020 Yevgeny Krasik                                   *
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

package com.gitlab.ykrasik.gamedex.core.task.presenter

import com.gitlab.ykrasik.gamedex.app.api.task.TaskProgress
import com.gitlab.ykrasik.gamedex.app.api.task.TaskView
import com.gitlab.ykrasik.gamedex.core.EventBus
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import com.gitlab.ykrasik.gamedex.core.awaitEvent
import com.gitlab.ykrasik.gamedex.core.task.ExpectedException
import com.gitlab.ykrasik.gamedex.core.task.Task
import com.gitlab.ykrasik.gamedex.core.task.TaskEvent
import com.gitlab.ykrasik.gamedex.util.Try
import com.gitlab.ykrasik.gamedex.util.humanReadable
import com.gitlab.ykrasik.gamedex.util.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.measureTimedValue

/**
 * User: ykrasik
 * Date: 30/10/2018
 * Time: 15:21
 */
@Singleton
class TaskPresenter @Inject constructor(private val eventBus: EventBus) : Presenter<TaskView> {
    private val log = logger()

    override fun present(view: TaskView) = object : ViewSession() {
        private val job get() = view.job.peek()

        init {
            eventBus.forEach<TaskEvent.RequestStart<*>> { execute(it.task) }
            view.cancelTaskActions.forEach { cancelTask() }
        }

        private suspend fun <T> execute(task: Task<T>) {
            if (job != null) {
                log.warn("Trying to execute a task(${task.title}) when one is already executing(${view.taskProgress.title})")
                eventBus.awaitEvent<TaskEvent.Finished<T>>()
                log.warn("Previous task(${view.taskProgress.title}) finished, executing new task(${task.title})")
            }
            check(job == null) { "Already running a job: ${view.taskProgress.title}" }

            view.isCancellable *= task.isCancellable
            view.isRunningSubTask *= false
            bindTaskProgress(task, view.taskProgress)
            // TODO: It's possible that before this coroutine is launched the subtask will send some messages to its message channel that will be lost.
            task.subTask.forEach { subTask ->
                if (subTask != null) {
                    bindTaskProgress(subTask, view.subTaskProgress)
                }
                view.isRunningSubTask *= subTask != null
            }

            val (result, timeTaken) = measureTimedValue {
                val deferred = GlobalScope.async(Dispatchers.IO) {
                    task.execute()
                }
                view.job *= deferred
                Try { deferred.await() }
            }

            view.job *= null
            view.isCancellable *= false
            view.isRunningSubTask *= false
            view.taskProgress.image *= null
            view.subTaskProgress.image *= null

            var resultToReturn = result
            when (result) {
                is Try.Success -> {
                    view.taskProgress.progress *= 1.0
                    view.subTaskProgress.progress *= 1.0

                    val successMessage = task.successMessage?.invoke(result.value)
                    successMessage?.let { view.taskSuccess(title = task.title, message = it) }
                    log.info("${successMessage ?: "${task.title} Done:"} [${timeTaken.humanReadable}]")
                }
                is Try.Error -> {
                    val error = result.error
                    when {
                        error is CancellationException && error !is ClosedSendChannelException -> {
                            val cancelMessage = task.cancelMessage?.invoke()
                            cancelMessage?.let { view.taskCancelled(title = task.title, message = it) }
                            log.info("${cancelMessage ?: "${task.title} Cancelled:"} [${timeTaken.humanReadable}]")

                            // Cancellation exceptions should not be treated as unexpected errors.
                            resultToReturn = Try.error(ExpectedException(error))
                        }
                        else -> {
                            val errorMessage = task.errorMessage?.invoke(error)
                            if (errorMessage != null) {
                                // The presence of an error message means the taskView should display this error, and not treat it as an unexpected error.
                                view.taskError(title = task.title, error = error, message = errorMessage)
                                resultToReturn = Try.error(ExpectedException(error))
                            }
                            log.error("${errorMessage ?: "${task.title} Error:"} [${timeTaken.humanReadable}]", error)
                        }
                    }
                }
            }

            eventBus.send(TaskEvent.Finished(task, resultToReturn))
        }

        private fun bindTaskProgress(task: Task<*>, taskProgress: TaskProgress) {
            taskProgress.title *= task.title

            taskProgress.message.bind(task.message) { msg ->
                log.info(msg)
            }
            task.message *= task.title

            taskProgress.totalItems.bind(task.totalItems)

            taskProgress.processedItems.bind(task.processedItemsChannel) { processedItems ->
                // FIXME: Remove this once StateFlow is available
                if (!task.totalItems.isClosed) {
                    val totalItems = task.totalItems.value
                    if (totalItems > 1) {
                        taskProgress.progress *= processedItems.toDouble() / totalItems
//                    log.debug("Progress: $processedItems/${task.totalItems} ${String.format("%.3f", taskProgress.progress * 100)}%")
                    }
                }
            }

            taskProgress.progress *= -1.0

            taskProgress.image.bind(task.image)
        }

        private fun cancelTask() {
            val job = checkNotNull(job) { "Cannot cancel, not running any job!" }
            check(view.isCancellable.value) { "Cannot cancel, current job is non-cancellable: $job" }
            job.cancel()
        }
    }
}