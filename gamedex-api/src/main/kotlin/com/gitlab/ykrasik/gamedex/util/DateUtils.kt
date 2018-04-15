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

package com.gitlab.ykrasik.gamedex.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate

/**
 * User: ykrasik
 * Date: 28/05/2017
 * Time: 22:05
 */
val now: DateTime get() = DateTime.now(DateTimeZone.UTC)
val today: LocalDate get() = LocalDate.now(DateTimeZone.UTC)

fun Long.toDateTime(): DateTime = DateTime(this, DateTimeZone.UTC)
fun String.toDate(): LocalDate = LocalDate.parse(this)

fun LocalDate.toJava(): java.time.LocalDate = java.time.LocalDate.of(year, monthOfYear, dayOfMonth)
fun java.time.LocalDate.toJoda(): LocalDate = LocalDate(year, monthValue, dayOfMonth)