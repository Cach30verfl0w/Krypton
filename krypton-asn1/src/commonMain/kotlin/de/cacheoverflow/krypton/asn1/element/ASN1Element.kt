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
import kotlinx.io.*
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * TODO: Add support for high-tag number bytes
 *
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@JvmInline
value class ASN1ParserContext private constructor(private val factories: List<ASN1ElementFactory<*>>) {

    /**
     * This function reads the tag component from the data and tries to find a valid element factory. It found, we parse the length of the
     * data and read the data. After that, we pass the data to the factory.
     *
     * @param source The source to read the data from
     * @return       The ASN.1 element if parsed
     *
     * @author Cedric Hammes
     * @since  29/12/2024
     */
    fun readObject(source: ByteArray): ASN1Element = readObject(Buffer().also { it.write(source) })

    /**
     * This function reads the tag component from the data and tries to find a valid element factory. It found, we parse the length of the
     * data and read the data. After that, we pass the data to the factory.
     *
     * @param source The source to read the data from
     * @return       The ASN.1 element if parsed
     *
     * @author Cedric Hammes
     * @since  29/12/2024
     */
    fun readObject(source: Source): ASN1Element {
        fun Source.readASN1Length(): Long {
            val lengthByte = readByte().toInt()
            return if (lengthByte and 0x80 == 0)
                lengthByte.toLong()
            else {
                val lengthOfLength = lengthByte and 0x7F
                var lengthValue: Long = 0
                for (i in 0 until lengthOfLength) {
                    lengthValue = (lengthValue shl 8) or (readByte().toLong() and 0xFF)
                }
                lengthValue
            }
        }

        // Acquire tag type and tag class. If factory found, read the data and construct object
        val tag = source.readUByte()
        val tagClass = EnumTagClass.byByte((tag.toInt() shr 6).toByte())
        val constructed = ((tag.toInt() and 0xFF) shr 4) and 0b1 == 1
        val tagType = (tag.toInt() and 0x1F).toByte()

        if (tagClass == EnumTagClass.CONTEXT_SPECIFIC)
            return ASN1ContextSpecificElement.fromData(
                context = this,
                constructed = constructed,
                tag = tag.toByte(),
                data = Buffer().also { it.write(source.readByteArray(source.readASN1Length().toInt())) }
            )
        else {
            val factory = factories.firstOrNull { it.tagClass == tagClass && it.tagType == tagType && it.isConstructed == constructed }
                ?: throw IllegalArgumentException("Illegal type $tagType with class $tagClass (constructed = $constructed)")
            return requireNotNull(factory.fromData(
                context = this,
                elementData = Buffer().also { it.write(source.readByteArray(source.readASN1Length().toInt())) }
            ))
        }
    }

    companion object {
        @JvmStatic
        fun default(): ASN1ParserContext =
            ASN1ParserContext(
                listOf(
                    ASN1Sequence,
                    ASN1Integer,
                    ASN1ObjectIdentifier,
                    ASN1Null.Factory,
                    ASN1OctetString,
                    ASN1Set,
                    ASN1PrintableString,
                    ASN1UTF8String,
                    ASN1UTCTime,
                    ASN1BitString
                )
            )
    }
}

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
interface ASN1ElementFactory<T : ASN1Element> {
    val tagClass: EnumTagClass
    val tagType: Byte
    val isConstructed: Boolean
    val tag: Byte get() = ((tagClass.value.toInt() shl 6) or (if (isConstructed) 0x20 else 0) or tagType.toInt()).toByte()

    fun fromData(context: ASN1ParserContext, elementData: Buffer): T

}

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
interface ASN1Element {
    fun write(sink: Sink)

    fun Sink.writeASN1Length(length: Long) {
        if (length <= 0x7F) {
            writeByte(length.toByte())
            return
        }

        val buffer = Buffer()
        var tempValue = length
        while (tempValue > 0) {
            buffer.writeByte((tempValue and 0xFF).toByte())
            tempValue = tempValue shr 8
        }

        writeByte((buffer.size or 0x80).toByte())
        write(buffer.readByteArray().reversedArray())
    }
}
