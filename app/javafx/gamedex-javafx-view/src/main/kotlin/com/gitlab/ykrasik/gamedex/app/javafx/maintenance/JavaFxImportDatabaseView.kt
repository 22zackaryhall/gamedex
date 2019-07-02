/****************************************************************************
 * Copyright (C) 2016-2019 Yevgeny Krasik                                   *
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

package com.gitlab.ykrasik.gamedex.app.javafx.maintenance

import com.gitlab.ykrasik.gamedex.app.api.maintenance.ImportDatabaseView
import com.gitlab.ykrasik.gamedex.app.api.util.channel
import com.gitlab.ykrasik.gamedex.javafx.control.*
import com.gitlab.ykrasik.gamedex.javafx.state
import com.gitlab.ykrasik.gamedex.javafx.theme.Icons
import com.gitlab.ykrasik.gamedex.javafx.theme.browseButton
import com.gitlab.ykrasik.gamedex.javafx.userMutableState
import com.gitlab.ykrasik.gamedex.javafx.view.ConfirmationWindow
import com.gitlab.ykrasik.gamedex.util.IsValid
import tornadofx.*
import java.io.File

/**
 * User: ykrasik
 * Date: 06/05/2019
 * Time: 08:38
 */
class JavaFxImportDatabaseView : ConfirmationWindow("Import Database", Icons.import), ImportDatabaseView {
    override val importDatabaseFile = userMutableState("")
    override val importDatabaseFileIsValid = state(IsValid.valid)

    override val shouldImportLibrary = userMutableState(false)
    override val canImportLibrary = state(IsValid.valid)

    override val shouldImportProviderAccounts = userMutableState(false)
    override val canImportProviderAccounts = state(IsValid.valid)

    override val shouldImportFilters = userMutableState(false)
    override val canImportFilters = state(IsValid.valid)

    override val browseActions = channel<Unit>()

    init {
        register()
    }

    override val root = borderpane {
        top = confirmationToolbar()
        center {
            form {
                minWidth = 600.0
                fieldset("The existing database will be lost!", Icons.warning)
                fieldset {
                    pathField()
                }
                fieldset("Import") {
                    horizontalField("Library") {
                        jfxCheckBox(shouldImportLibrary.property) {
                            enableWhen(canImportLibrary)
                        }
                    }
                    horizontalField("Provider Accounts") {
                        jfxCheckBox(shouldImportProviderAccounts.property) {
                            enableWhen(canImportProviderAccounts)
                        }
                    }
                    horizontalField("Filters") {
                        jfxCheckBox(shouldImportFilters.property) {
                            enableWhen(canImportFilters)
                        }
                    }
                }
            }
        }
    }

    private fun Fieldset.pathField() = horizontalField("Path") {
        jfxTextField(importDatabaseFile.property, promptText = "Enter Path...") {
            validWhen(importDatabaseFileIsValid)
        }
        browseButton { action(browseActions) }
    }


    override fun selectImportDatabaseFile(initialDirectory: File?) =
        chooseFile("Select Database File...", filters = emptyArray()) {
            this@chooseFile.initialDirectory = initialDirectory
        }.firstOrNull()
}