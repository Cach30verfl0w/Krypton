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

package de.cacheoverflow.krypton.asn1.element

import de.cacheoverflow.krypton.asn1.EnumTagClass
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readByteArray
import kotlin.jvm.JvmStatic

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
class ASN1UTCTime(var value: LocalDateTime) : ASN1Element {

    override fun write(sink: Sink) {
        sink.writeByte(tag)
        val data = TIME_FORMAT.format(value).encodeToByteArray()
        sink.writeASN1Length(data.size.toLong())
        sink.write(data)
    }

    override fun toString(): String = "UTCTime(${value})"

    companion object : ASN1ElementFactory<ASN1UTCTime> {
        @JvmStatic val TIME_FORMAT: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
            yearTwoDigits(2000)
            monthNumber()
            dayOfMonth()
            hour()
            minute()
            second()
            char('Z')
        }

        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 0x17
        @JvmStatic override val isConstructed: Boolean = false
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): Result<ASN1UTCTime> =
            Result.success(ASN1UTCTime(TIME_FORMAT.parse(elementData.readByteArray().decodeToString())))

        @JvmStatic
        fun fromString(value: String): ASN1UTCTime = ASN1UTCTime(TIME_FORMAT.parse(value))
    }
}
