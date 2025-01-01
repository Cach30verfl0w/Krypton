/*
 * Copyright 2024 Cedric Hammes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cacheoverflow.krypton.asn1

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlin.jvm.JvmStatic

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate", "Unused")
class ASN1UtcTime(var value: LocalDateTime) : ASN1Element {
    constructor(value: String) : this(TIME_FORMAT.parse(value))

    override fun write(sink: Sink) {
        sink.writeByte(tag.value)
        val data = TIME_FORMAT.format(value).encodeToByteArray()
        sink.writeASN1Length(data.size.toLong())
        sink.write(data)
    }

    override fun asCollection(): ASN1Collection<*> =
        throw UnsupportedOperationException("Unable to convert UTC time to collection")
    override fun asString(): String =
        throw UnsupportedOperationException("Unable to convert UTC time to string")
    override fun asAny(): Any = value

    companion object : ASN1Element.Factory<ASN1UtcTime> {
        override val tag: ASN1Element.ASN1Tag = ASN1Element.ASN1Tag.UTC_TIME
        @JvmStatic val TIME_FORMAT: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
            yearTwoDigits(2000)
            monthNumber()
            dayOfMonth()
            hour()
            minute()
            second()
            char('Z')
        }

        @JvmStatic
        override fun fromData(source: Source, length: Long): Result<ASN1UtcTime> =
            Result.success(ASN1UtcTime(TIME_FORMAT.parse(source.readByteArray(length.toInt()).decodeToString())))
    }
}
