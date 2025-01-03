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

import de.cacheoverflow.krypton.asn1.ASN1PrintableString.Companion.readASN1Length
import de.cacheoverflow.krypton.asn1.serialization.ASN1Decoder
import de.cacheoverflow.krypton.asn1.serialization.ASN1Encoder
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

internal class ASN1ElementSerializer : KSerializer<ASN1Element> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(requireNotNull(ASN1Element::class.qualifiedName))
    override fun deserialize(decoder: Decoder): ASN1Element = when(decoder) {
        is ASN1Decoder -> decoder.root.removeFirst()
        else -> throw IllegalArgumentException("Unable to decoder with ${decoder::class.qualifiedName}")
    }
    override fun serialize(encoder: Encoder, value: ASN1Element) = when(encoder) {
        is ASN1Encoder -> encoder.sequence += value
        else -> throw IllegalArgumentException("Unable to encode with ${encoder::class.qualifiedName}")
    }
}

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Serializable(with = ASN1ElementSerializer::class)
sealed interface ASN1Element {
    fun write(sink: Sink)

    fun asCollection(): ASN1Collection<*>
    fun asString(): String
    fun asAny(): Any

    /**
     * @author Cedric Hammes
     * @since  29/12/2024
     */
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

    /**
     * This value is an inline wrapper around ASN.1 tags. These tags are used as identifier for the object being
     * parsed.
     *
     * @param value The byte value containing all values of an ASN.1 tag
     *
     * @author Cedric Hammes
     * @since  31/12/2024
     */
    @JvmInline
    @Suppress("Unused")
    value class ASN1Tag(val value: Byte) {
        constructor(id: Byte, kind: EnumTagKind, constructed: Boolean) :
                this(((kind.value.toInt() shl 6) or (if (constructed) 0x20 else 0) or id.toInt()).toByte())
        constructor(source: Source) : this(source.readByte())

        inline val kind: EnumTagKind get() = EnumTagKind.byByte((value.toInt() shr 6).toByte())
        inline val constructed: Boolean get() = (value.toInt() shr 6) and 0b1 == 1
        inline val identifier: Byte get() = (value.toInt() and 0x1F).toByte()
        inline fun isList(): Boolean = isSet() || isSequence()
        inline fun isSequence(): Boolean = this == SEQUENCE
        inline fun isSet(): Boolean = this == SET

        @OptIn(ExperimentalStdlibApi::class)
        override fun toString(): String = "0x${identifier.toHexString()} ($kind)"
        inline fun findFactory(): Factory<*>? = when(this) {
            NULL -> ASN1Null
            SET -> ASN1Set
            SEQUENCE -> ASN1Sequence
            PRINTABLE_STRING -> ASN1PrintableString
            T61_STRING -> ASN1T61String
            IA5_STRING -> ASN1IA5String
            UTF8_STRING -> ASN1Utf8String
            BIT_STRING -> ASN1BitString
            UTC_TIME -> ASN1UtcTime
            INTEGER -> ASN1Integer
            OBJECT_ID -> ObjectIdentifier
            OCTET_STRING -> ASN1OctetString
            else -> null
        }

        companion object {
            // @formatter:off
            @JvmStatic val INTEGER: ASN1Tag          = ASN1Tag(0x02, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val BIT_STRING: ASN1Tag       = ASN1Tag(0x03, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val OCTET_STRING: ASN1Tag     = ASN1Tag(0x04, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val NULL: ASN1Tag             = ASN1Tag(0x05, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val OBJECT_ID: ASN1Tag        = ASN1Tag(0x06, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val UTF8_STRING: ASN1Tag      = ASN1Tag(0x0C, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val SEQUENCE: ASN1Tag         = ASN1Tag(0x10, EnumTagKind.UNIVERSAL, true)
            @JvmStatic val SET: ASN1Tag              = ASN1Tag(0x11, EnumTagKind.UNIVERSAL, true)
            @JvmStatic val PRINTABLE_STRING: ASN1Tag = ASN1Tag(0x13, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val T61_STRING: ASN1Tag       = ASN1Tag(0x14, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val IA5_STRING: ASN1Tag       = ASN1Tag(0x16, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val UTC_TIME: ASN1Tag         = ASN1Tag(0x17, EnumTagKind.UNIVERSAL, false)
            @JvmStatic val UNIVERSAL_STRING: ASN1Tag = ASN1Tag(0x1C, EnumTagKind.UNIVERSAL, true)
            @JvmStatic val BMP_STRING: ASN1Tag       = ASN1Tag(0x1E, EnumTagKind.UNIVERSAL, true)
            // @formatter:on

            @JvmStatic
            fun fromClass(type: KClass<out ASN1Element>): ASN1Tag? = when(type) {
                ASN1Integer::class -> INTEGER
                ASN1BitString::class -> BIT_STRING
                ASN1OctetString::class -> OCTET_STRING
                ASN1Null::class -> NULL
                ObjectIdentifier::class -> OBJECT_ID
                ASN1Utf8String::class -> UTF8_STRING
                ASN1Sequence::class -> SEQUENCE
                ASN1Set::class -> SET
                ASN1PrintableString::class -> PRINTABLE_STRING
                ASN1T61String::class -> T61_STRING
                ASN1IA5String::class -> IA5_STRING
                ASN1UtcTime::class -> UTC_TIME
                ASN1UniversalString::class -> UNIVERSAL_STRING
                ASN1BMPString::class -> BMP_STRING
                else -> null
            }

            @JvmStatic
            fun read(source: Source): Result<ASN1Element> {
                val tag = ASN1Tag(source)
                val factory = requireNotNull(tag.findFactory()) { "Tag '$tag' is invalid" }
                return factory.fromData(source, source.readASN1Length())
            }
        }
    }

    /**
     * @author Cedric Hammes
     * @since  29/12/2024
     */
    interface Factory<T : ASN1Element> {
        val tag: ASN1Tag

        fun fromData(source: Source, length: Long): Result<T>

        fun fromSource(source: Source): Result<T> {
            val tag = ASN1Tag(source)
            if (tag != this.tag)
                return Result.failure(RuntimeException("Illegal tag '$tag', should be '${this.tag}'"))
            val length = source.readASN1Length()
            return fromData(source, length)
        }

        fun wrap(value: ASN1Element): T = throw UnsupportedOperationException("Unable to wrap element into '$tag'")

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
    }
}

/**
 * @author Cedric Hammes
 * @since  01/01/2024
 */
sealed interface ASN1ElementContainer : ASN1Element {
    fun unwrap(): ASN1Element
}

inline fun ASN1Element.asSequence(): ASN1Sequence = when(val collection = asCollection()) {
    is ASN1Sequence -> this as ASN1Sequence
    is ASN1Set -> ASN1Sequence(collection.children)
}

inline fun ASN1Element.asSet(): ASN1Set = when(val collection = asCollection()) {
    is ASN1Set -> this as ASN1Set
    is ASN1Sequence -> ASN1Set(collection.children)
}
