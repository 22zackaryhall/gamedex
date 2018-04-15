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

package com.gitlab.ykrasik.gamedex.javafx

import com.gitlab.ykrasik.gamedex.core.api.util.uiThreadDispatcher
import com.gitlab.ykrasik.gamedex.core.api.util.uiThreadScheduler
import com.gitlab.ykrasik.gamedex.javafx.preloader.PreloaderView
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import kotlinx.coroutines.experimental.javafx.JavaFx
import tornadofx.App
import tornadofx.launch

/**
 * User: ykrasik
 * Date: 08/10/2016
 * Time: 21:40
 */
class Main : App(PreloaderView::class) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            uiThreadScheduler = JavaFxScheduler.platform()
            uiThreadDispatcher = JavaFx
            launch<Main>(args)
        }
    }
}