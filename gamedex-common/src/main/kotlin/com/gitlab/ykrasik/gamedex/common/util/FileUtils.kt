package com.gitlab.ykrasik.gamedex.common.util

import java.io.File

/**
 * User: ykrasik
 * Date: 02/10/2016
 * Time: 19:52
 */
fun String.toFile(): File = File(this)

fun File.create() {
    parentFile.mkdirs()
    createNewFile()
}

fun File.existsOrNull(): File? = if (exists()) this else null