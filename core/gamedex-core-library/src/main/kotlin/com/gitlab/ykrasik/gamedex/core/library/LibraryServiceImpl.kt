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

package com.gitlab.ykrasik.gamedex.core.library

import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.LibraryData
import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.app.api.util.task
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 26/04/2018
 * Time: 19:34
 */
@Singleton
class LibraryServiceImpl @Inject constructor(private val repo: LibraryRepository) : LibraryService {
    override val libraries = repo.libraries

    override fun get(id: Int) = libraries.find { it.id == id }
        ?: throw IllegalArgumentException("Library doesn't exist: id=$id")

    override fun get(platform: Platform, name: String) = doGet(platform, name)
        ?: throw IllegalArgumentException("Library doesn't exist: platform=$platform, name=$name")

    override fun add(data: LibraryData) = task("Adding Library '${data.name}'...") {
        successMessage = { "Added Library: '${data.name}'." }
        repo.add(data)
    }

    override fun addAll(data: List<LibraryData>) = task("Adding ${data.size} Libraries...") {
        successMessage = { "Added $processedItems/$totalItems Libraries." }
        totalItems = data.size
        repo.addAll(data) { incProgress() }
    }

    override fun replace(library: Library, data: LibraryData) = task("Updating Library '${library.name}'...") {
        val updatedLibrary = repo.update(library, data)
        successMessage = { "Updated Library: '${updatedLibrary.name}'." }
    }

    override fun delete(library: Library) = task("Deleting Library '${library.name}'...") {
        successMessage = { "Deleted Library: '${library.name}'." }
        repo.delete(library)
    }

    override fun deleteAll(libraries: List<Library>) = task("Deleting ${libraries.size} Libraries...") {
        successMessage = { "Deleted ${libraries.size} Libraries." }
        repo.deleteAll(libraries)
    }

    override fun invalidate() = repo.invalidate()

    override fun isAvailableNewName(platform: Platform, newName: String): Boolean =
        doGet(platform, newName) == null

    override fun isAvailableUpdatedName(library: Library, updatedName: String): Boolean =
        doGet(library.platform, updatedName) ?: library == library

    override fun isAvailableNewPath(newPath: File): Boolean =
        doGet(newPath) == null

    override fun isAvailableUpdatedPath(library: Library, updatedPath: File): Boolean =
        doGet(updatedPath) ?: library == library

    private fun doGet(platform: Platform, name: String) = libraries.find { it.platform == platform && it.name == name }
    private fun doGet(path: File) = libraries.find { it.path == path }
}