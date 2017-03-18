package com.gitlab.ykrasik.gamedex.common.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

/**
 * User: ykrasik
 * Date: 25/05/2016
 * Time: 10:02
 */
class KLogger(loggerName: String) {
    val logger: Logger = LoggerFactory.getLogger(if (loggerName.endsWith("Impl")) loggerName.dropLast(4) else loggerName)

    inline fun error(crossinline msg: () -> String): Unit { if (logger.isErrorEnabled) logger.error(msg()) }
    fun error(t: Throwable, msg: () -> String = { "Error!" }): Unit { logger.error(msg(), t) }
    inline fun warn(crossinline msg: () -> String): Unit { if (logger.isWarnEnabled) logger.warn(msg()) }
    inline fun info( crossinline msg: () -> String): Unit { if (logger.isInfoEnabled) logger.info(msg()) }
    inline fun debug(crossinline msg: () -> String): Unit { if (logger.isDebugEnabled) logger.debug(msg()) }
    inline fun trace(crossinline msg: () -> String): Unit { if (logger.isTraceEnabled) logger.trace(msg()) }

    inline fun <T> logIfError(crossinline f: () -> T): T = try {
        f()
    } catch (t: Throwable) {
        error(t)
        throw t
    }

    suspend fun <T> logIfErrorSuspend(f: suspend () -> T): T = try {
        f()
    } catch (t: Throwable) {
        error(t)
        throw t
    }

    // For delegation access.
    operator fun getValue(thisRef: Any, property: KProperty<*>) = this
}

fun <R : Any> R.logger() = KLogger(unwrapCompanionClass(this.javaClass).name)

// unwrap companion class to enclosing class given a Java Class
private fun <T: Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}