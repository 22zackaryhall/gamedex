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

package com.gitlab.ykrasik.gamedex.core.web.presenter

import com.gitlab.ykrasik.gamedex.app.api.web.ViewCanBrowseUrl
import com.gitlab.ykrasik.gamedex.core.Presenter
import com.gitlab.ykrasik.gamedex.core.ViewSession
import java.awt.Desktop
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User: ykrasik
 * Date: 12/10/2018
 * Time: 09:48
 */
@Singleton
class BrowseUrlPresenter @Inject constructor() : Presenter<ViewCanBrowseUrl> {
    override fun present(view: ViewCanBrowseUrl) = object : ViewSession() {
        init {
            view.browseUrlActions.forEach {
                val uri = URI(it)
                Desktop.getDesktop().browse(uri)
            }
        }
    }
}