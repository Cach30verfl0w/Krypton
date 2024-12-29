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
value class ASN1ObjectIdentifier(val identifier: List<Int>) : ASN1Element {
    override fun write(sink: Sink) = TODO("Not implemented yet")
    override fun toString(): String = "Identifier(${identifier.joinToString(".")})"

    companion object : ASN1ElementFactory<ASN1ObjectIdentifier> {
        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 6
        @JvmStatic override val isConstructed: Boolean = false
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): ASN1ObjectIdentifier {
            val firstByte = elementData.readByte()
            val list = mutableListOf(firstByte / 40, firstByte % 40)
            while (elementData.size > 0) {
                var value = 0
                var byte: Int
                do {
                    byte = elementData.readByte().toInt() and 0xFF
                    value = (value shl 7) or (byte and 0x7F)
                } while (byte and 0x80 != 0)
                list.add(value)
            }
            return ASN1ObjectIdentifier(list)
        }

        @JvmStatic
        fun fromString(value: String): ASN1ObjectIdentifier = ASN1ObjectIdentifier(value.split(".").map { it.toInt() })
    }
}
