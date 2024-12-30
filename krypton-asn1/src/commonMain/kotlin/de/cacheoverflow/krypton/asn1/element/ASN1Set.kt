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
 * @since  30/12/2024
 */
@JvmInline
@Suppress("MemberVisibilityCanBePrivate")
value class ASN1Set private constructor(val children: MutableList<ASN1Element>) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag)
        val buffer = Buffer().also { buffer -> children.forEach { it.write(buffer) } }
        sink.writeASN1Length(buffer.size)
        sink.write(buffer, buffer.size)
    }

    companion object : ASN1ElementFactory<ASN1Set> {
        // @formatter:off
        @JvmStatic override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        @JvmStatic override val tagType: Byte = 0x11
        @JvmStatic override val isConstructed: Boolean = true
        // @formatter:on

        @JvmStatic
        override fun fromData(context: ASN1ParserContext, elementData: Buffer): ASN1Set =
            ASN1Set(mutableListOf<ASN1Element>().also { children ->
                while (elementData.size > 0L) {
                    val child = context.readObject(elementData)
                    children.add(child)
                }
            })
    }
}
