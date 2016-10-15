package com.gitlab.ykrasik.gamedex.core.scan

import com.github.ykrasik.gamedex.common.children
import com.github.ykrasik.gamedex.common.exists
import com.github.ykrasik.gamedex.common.isDirectory
import com.github.ykrasik.gamedex.common.logger
import com.github.ykrasik.gamedex.datamodel.HasPath
import com.gitlab.ykrasik.gamedex.core.ui.ExcludedPathUIManager
import com.gitlab.ykrasik.gamedex.core.ui.GameUIManager
import com.gitlab.ykrasik.gamedex.core.ui.LibraryUIManager
import com.gitlab.ykrasik.gamedex.core.ui.UIManager
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 15/10/2016
 * Time: 13:16
 *
 * Will recursively scan any directory containing only directories.
 * Will not recurse into a directory containing files.
 *
 * This is a simple algorithm that doesn't check file extensions, if it proves to be too simple (stupid), update this
 * class to only consider file extensions.
 */
@Singleton
class PathDetector @Inject constructor(
    private val gameUiManager: GameUIManager,
    private val libraryUiManager: LibraryUIManager,
    private val excludedPathUiManager: ExcludedPathUIManager
) {
    private val log by logger()

    fun detectNewPaths(path: Path): List<Path> {
        if (!path.exists) {
            log.warn { "Path doesn't exist: $path" }
            return emptyList()
        }

        val children = path.children
        val shouldScanRecursively = shouldScanRecursively(children)
        return if (shouldScanRecursively) {
            children.flatMap { detectNewPaths(it) }
        } else {
            if (isPathKnown(path)) {
                emptyList()
            } else {
                listOf(path)
            }
        }
    }

    // Scan children recursively if all children are directories.
    private fun shouldScanRecursively(children: List<Path>): Boolean = children.isNotEmpty() && children.all(Path::isDirectory)

    fun isPathKnown(path: Path): Boolean {
        if (check(path, gameUiManager, "an already mapped game")) return true
        if (check(path, libraryUiManager, "an already mapped library")) return true
        if (check(path, excludedPathUiManager, "an excluded path")) return true

        log.info { "[$path] is a new path!" }
        return false
    }

    private fun <T : HasPath> check(path: Path, checker: UIManager<T>, message: String): Boolean = if (path in checker) {
        log.debug { "[$path] is $message." }
        true
    } else {
        false
    }
}