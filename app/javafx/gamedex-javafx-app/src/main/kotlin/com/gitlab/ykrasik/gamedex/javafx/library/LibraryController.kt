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

package com.gitlab.ykrasik.gamedex.javafx.library

import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryService
import com.gitlab.ykrasik.gamedex.javafx.toObservableList
import tornadofx.Controller
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 23/04/2017
 * Time: 11:05
 */
// FIXME: Get rid of this class.
@Deprecated("Should be handled by LibraryPresenter.")
@Singleton
class LibraryController @Inject constructor(libraryService: LibraryService) : Controller() {
    val realLibraries = libraryService.realLibraries.toObservableList()
}