package com.gitlab.ykrasik.gamedex.persistence.dao

import com.github.ykrasik.gamedex.common.logger
import com.github.ykrasik.gamedex.common.toId
import com.github.ykrasik.gamedex.common.toPath
import com.github.ykrasik.gamedex.datamodel.persistence.ExcludedPath
import com.gitlab.ykrasik.gamedex.persistence.entity.ExcludedPaths
import com.gitlab.ykrasik.gamedex.persistence.entity.selectBy
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 02/10/2016
 * Time: 15:37
 */
interface ExcludedPathDao {
    val all: List<ExcludedPath>

    fun exists(path: Path): Boolean

    fun add(path: Path): ExcludedPath

    fun delete(path: ExcludedPath)
}

@Singleton
class ExcludedPathDaoImpl @Inject constructor() : ExcludedPathDao {
    private val log by logger()

    override val all: List<ExcludedPath> get() {
        log.info { "Fetching all..." }
        val excludedPaths = transaction {
            ExcludedPaths.selectAll().map { it.toExcludedPath() }
        }
        log.info { "Result: ${excludedPaths.size} excluded paths." }
        return excludedPaths
    }

    override fun exists(path: Path): Boolean {
        log.debug { "Checking if exists: '$path'..." }
        val contains = transaction {
            !ExcludedPaths.selectBy { it.path.eq(path.toString()) }.empty()
        }
        log.debug { "Result: $contains." }
        return contains
    }

    override fun add(path: Path): ExcludedPath {
        log.info { "Inserting: '$path'..." }
        val id = transaction {
            ExcludedPaths.insert {
                it[ExcludedPaths.path] = path.toString()
            } get ExcludedPaths.id
        }
        val excludedPath = ExcludedPath(id.toId(), path)
        log.info { "Result: $excludedPath." }
        return excludedPath
    }

    override fun delete(path: ExcludedPath) {
        log.info { "Deleting: $path..." }
        val amount = transaction {
            ExcludedPaths.deleteWhere { ExcludedPaths.id.eq(path.id.id) }
        }
        require(amount == 1) { "ExcludedPath doesn't exist: $path" }
        log.info { "Done." }
    }

    private fun ResultRow.toExcludedPath() = ExcludedPath(
        id = this[ExcludedPaths.id].toId(),
        path = this[ExcludedPaths.path].toPath()
    )
}