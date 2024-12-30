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
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readByteArray
import kotlin.jvm.JvmStatic

/**
 * TODO: Invalid parsing or writing
 *
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
class ASN1Integer private constructor(var value: Int) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag)
        val buffer = Buffer()
        var remainingValue = value
        while (remainingValue != 0) {
            val byte = (remainingValue and 0xFF).toByte()
            buffer.writeByte(byte)
            remainingValue = remainingValue ushr 8
        }
        sink.writeASN1Length(buffer.size)
        sink.write(buffer.readByteArray().reversedArray())
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = "Int(${value.toHexString()})"

    companion object : ASN1ElementFactory<ASN1Integer> {
        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 2
        @JvmStatic override val isConstructed: Boolean = false
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): ASN1Integer {
            var value = 0
            for (i in 0..<elementData.size) {
                value = (value shl 8) or (elementData.readByte().toInt() and 0xFF)
            }
            return ASN1Integer(value)
        }
    }
}
