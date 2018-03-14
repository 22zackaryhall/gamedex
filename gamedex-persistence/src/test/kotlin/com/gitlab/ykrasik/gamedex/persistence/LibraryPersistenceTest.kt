package com.gitlab.ykrasik.gamedex.persistence

import com.gitlab.ykrasik.gamedex.Game
import com.gitlab.ykrasik.gamedex.Library
import com.gitlab.ykrasik.gamedex.Platform
import com.gitlab.ykrasik.gamedex.test.randomFile
import com.gitlab.ykrasik.gamedex.test.randomPath
import com.gitlab.ykrasik.gamedex.util.toFile
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import org.h2.jdbc.JdbcSQLException

/**
 * User: ykrasik
 * Date: 23/04/2017
 * Time: 13:50
 */
class LibraryPersistenceTest : AbstractPersistenceTest() {
    init {
        "Insert" should {
            "insert and retrieve a single library" test {
                val path = randomFile()
                val data = libraryData()

                val library = persistenceService.insertLibrary(path, data)

                library.path shouldBe path
                library.data shouldBe data

                fetchLibraries() shouldBe listOf(library)
            }

            "insert and retrieve multiple libraries" test {
                val library1 = insertLibrary()
                val library2 = insertLibrary()

                fetchLibraries() shouldBe listOf(library1, library2)
            }

            "throw an exception when trying to insert a library at the same path twice" test {
                val path = randomPath()
                givenLibrary(path = path)

                shouldThrow<JdbcSQLException> {
                    insertLibrary(path = path)
                }
            }
        }

        "Update" should {
            "update a library's path & data" test {
                val library = givenLibrary(platform = Platform.pc)
                val updatedLibrary = library.copy(
                    path = (library.path.toString() + "a").toFile(),
                    data = library.data.copy(platform = Platform.android, name = library.name + "b"))

                persistenceService.updateLibrary(updatedLibrary)

                fetchLibraries() shouldBe listOf(updatedLibrary)
            }

            "throw an exception when trying to update a library's path to one that already exists" test {
                val library1 = givenLibrary()
                val library2 = givenLibrary()

                val updatedLibrary = library2.copy(path = library1.path)

                shouldThrow<JdbcSQLException> {
                    persistenceService.updateLibrary(updatedLibrary)
                }
            }
        }

        "Delete" should {
            "delete existing libraries" test {
                val library1 = givenLibrary()
                val library2 = givenLibrary()

                persistenceService.deleteLibrary(library1.id)
                fetchLibraries() shouldBe listOf(library2)

                persistenceService.deleteLibrary(library2.id)
                fetchLibraries() shouldBe emptyList<Library>()
            }

            "throw an exception when trying to delete a library that doesn't exist" test {
                val library = givenLibrary()

                shouldThrow<IllegalArgumentException> {
                    persistenceService.deleteLibrary(library.id + 1)
                }
            }
        }

        "BatchDelete" should {
            "batch delete libraries by id" test {
                val library1 = givenLibrary()
                val library2 = givenLibrary()
                val library3 = givenLibrary()
                val library4 = givenLibrary()

                persistenceService.deleteLibraries(emptyList()) shouldBe 0
                fetchLibraries() shouldBe listOf(library1, library2, library3, library4)

                persistenceService.deleteLibraries(listOf(999)) shouldBe 0
                fetchLibraries() shouldBe listOf(library1, library2, library3, library4)

                persistenceService.deleteLibraries(listOf(library1.id, library3.id, 999)) shouldBe 2
                fetchLibraries() shouldBe listOf(library2, library4)

                persistenceService.deleteLibraries(listOf(library2.id)) shouldBe 1
                fetchLibraries() shouldBe listOf(library4)

                persistenceService.deleteLibraries(listOf(library4.id)) shouldBe 1
                fetchLibraries() shouldBe emptyList<Game>()

                persistenceService.deleteLibraries(listOf(library4.id)) shouldBe 0
                fetchLibraries() shouldBe emptyList<Game>()
            }
        }
    }

    private infix fun String.test(test: LibraryScope.() -> Unit) = inScope(::LibraryScope, test)
}