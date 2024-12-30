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
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
class ASN1PrintableString private constructor(var value: String) : ASN1Element {

    init {
        require(value.all { it in '\u0020'..'\u007E' }) { "Illegal symbols in string '$value'" }
    }

    override fun write(sink: Sink) {
        sink.writeByte(tag)
        val byteData = value.encodeToByteArray()
        sink.writeASN1Length(byteData.size.toLong())
        sink.write(byteData)
    }

    override fun toString(): String = "PrintableString(\"${value}\")"

    companion object : ASN1ElementFactory<ASN1PrintableString> {
        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 0x13
        @JvmStatic override val isConstructed: Boolean = true
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): ASN1PrintableString =
            ASN1PrintableString(elementData.readByteArray().decodeToString())

        @JvmStatic
        fun fromString(value: String): ASN1PrintableString = ASN1PrintableString(value)
    }
}
