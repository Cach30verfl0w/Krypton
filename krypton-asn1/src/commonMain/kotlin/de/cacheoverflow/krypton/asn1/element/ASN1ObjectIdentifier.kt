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
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@JvmInline
@Suppress("MemberVisibilityCanBePrivate")
value class ASN1ObjectIdentifier(val parts: List<Int>) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag)

        // Write OID and length into sink
        val buffer = Buffer()
        buffer.writeByte((parts[0] * 40 + parts[1]).toByte())
        for (i in 2 until parts.size) {
            var value = parts[i]
            val encodedBytes = mutableListOf<Byte>()
            do {
                encodedBytes.add(0, (value and 0x7F).toByte())
                value = value shr 7
            } while (value > 0)
            for (j in 0 until encodedBytes.size - 1) {
                encodedBytes[j] = (encodedBytes[j].toInt() or 0x80).toByte()
            }
            buffer.write(encodedBytes.toByteArray())
        }

        sink.writeASN1Length(buffer.size)
        sink.write(buffer, buffer.size)
    }

    override fun toString(): String = "Identifier(${parts.joinToString(".")})"

    companion object : ASN1ElementFactory<ASN1ObjectIdentifier> {
        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 6
        @JvmStatic override val isConstructed: Boolean = false
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): ASN1ObjectIdentifier {
            val firstByte = elementData.readByte()
            val parts = mutableListOf(firstByte / 40, firstByte % 40)
            while (elementData.size > 0) {
                var value = 0
                var byte: Int
                do {
                    byte = elementData.readByte().toInt() and 0xFF
                    value = (value shl 7) or (byte and 0x7F)
                } while (byte and 0x80 != 0)
                parts.add(value)
            }
            return ASN1ObjectIdentifier(parts)
        }

        @JvmStatic
        fun fromString(value: String): ASN1ObjectIdentifier = ASN1ObjectIdentifier(value.split(".").map { it.toInt() })
    }
}
