package com.gitlab.ykrasik.gamedex.persistence.module

import com.gitlab.ykrasik.gamedex.persistence.PersistenceConfig
import com.gitlab.ykrasik.gamedex.persistence.PersistenceService
import com.gitlab.ykrasik.gamedex.persistence.PersistenceServiceImpl
import com.gitlab.ykrasik.gamedex.persistence.dao.*
import com.google.inject.AbstractModule

/**
 * User: ykrasik
 * Date: 02/10/2016
 * Time: 15:22
 */
class PersistenceModule : AbstractModule() {
    override fun configure() {
        bind(PersistenceService::class.java).to(PersistenceServiceImpl::class.java)
        bind(PersistenceConfig::class.java).toInstance(PersistenceConfig())

        bind(GameDao::class.java).to(GameDaoImpl::class.java)
        bind(LibraryDao::class.java).to(LibraryDaoImpl::class.java)
        bind(ExcludedPathDao::class.java).to(ExcludedPathDaoImpl::class.java)
        bind(ImageDao::class.java).to(ImageDaoImpl::class.java)
    }
}