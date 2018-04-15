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

import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.core.api.game.GameRepository
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryEvent
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryPresenter
import com.gitlab.ykrasik.gamedex.core.api.library.LibraryRepository
import com.gitlab.ykrasik.gamedex.core.game.GameUserConfig
import com.gitlab.ykrasik.gamedex.core.userconfig.UserConfigRepository
import com.gitlab.ykrasik.gamedex.javafx.*
import com.gitlab.ykrasik.gamedex.javafx.dialog.areYouSureDialog
import com.gitlab.ykrasik.gamedex.javafx.task.JavaFxTaskRunner
import tornadofx.Controller
import tornadofx.label
import tornadofx.listview
import tornadofx.observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 23/04/2017
 * Time: 11:05
 */
// TODO: Move to tornadoFx di() and have the presenter as a dependency.
@Singleton
class LibraryController @Inject constructor(
    private val libraryPresenter: LibraryPresenter,
    private val libraryRepository: LibraryRepository,
    private val gameRepository: GameRepository,
    private val taskRunner: JavaFxTaskRunner,
    private val userConfigRepository: UserConfigRepository
) : Controller() {
    private val gameUserConfig = userConfigRepository[GameUserConfig::class]

    private val libraryView: LibraryScreen by inject()

    val allLibraries = libraryRepository.libraries.toObservableList()
    val realLibraries = allLibraries.filtered { it.platform != Platform.excluded }
    val platformLibraries = realLibraries.sortedFiltered().apply {
        predicateProperty.bind(gameUserConfig.platformSubject.toBindingCached().toPredicateF { platform, library: Library ->
            library.platform == platform
        })
    }

    init {
        libraryView.events.subscribe {
            when (it) {
                LibraryEvent.AddLibrary -> addLibrary()
                is LibraryEvent.EditLibrary -> edit(it.library)
                is LibraryEvent.DeleteLibrary -> delete(it.library)
            }
        }
    }

    private suspend fun addLibrary() {
        addOrEditLibrary<LibraryFragment.Choice.AddNewLibrary>(library = null) { choice ->
            libraryPresenter.addLibrary(choice.request)
        }
    }

    private suspend fun edit(library: Library) {
        addOrEditLibrary<LibraryFragment.Choice.EditLibrary>(library) { choice ->
            libraryPresenter.replaceLibrary(library, choice.library)
        }
    }

    private suspend inline fun <reified T : LibraryFragment.Choice> addOrEditLibrary(library: Library?,
                                                                                     noinline f: suspend (T) -> Unit): Boolean {
        val choice = LibraryFragment(library).show()
        if (choice === LibraryFragment.Choice.Cancel) return false

        f(choice as T)
        return true
    }

    private suspend fun delete(library: Library) {
        if (confirmDelete(library)) {
            libraryPresenter.deleteLibrary(library)
        }
    }

    private fun confirmDelete(library: Library): Boolean {
        val gamesToBeDeleted = gameRepository.games.mapNotNull { if (it.library.id == library.id) it.name else null }
        return areYouSureDialog("Delete library '${library.name}'?") {
            if (gamesToBeDeleted.isNotEmpty()) {
                label("The following ${gamesToBeDeleted.size} games will also be deleted:")
                listview(gamesToBeDeleted.observable()) { fitAtMost(10) }
            }
        }
    }

    fun getBy(platform: Platform, name: String) = libraryRepository[platform, name]
}