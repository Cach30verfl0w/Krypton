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

import de.cacheoverflow.krypton.asn1.serialization.ASN1Decoder
import de.cacheoverflow.krypton.asn1.serialization.ASN1Encoder
import kotlinx.io.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmStatic

class ObjectIdentifierSerializer : KSerializer<ObjectIdentifier> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(requireNotNull(ObjectIdentifier::class.qualifiedName), PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ObjectIdentifier) = when(encoder) {
        is ASN1Encoder -> encoder.sequence += value
        else -> encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ObjectIdentifier = when (decoder) {
        is ASN1Decoder -> ObjectIdentifier.fromSource(decoder.source).getOrThrow()
        else -> ObjectIdentifier(decoder.decodeString())
    }
}

/**
 * This element is representing an object identifier, a worldwide unique identifier referencing objects, concepts etc. standardized by the
 * [International Telecommunication Union](https://en.wikipedia.org/wiki/International_Telecommunication_Union).
 *
 * TODO: Add some validation (extras for string input)
 *
 * @param parts An array list containing the integer parts separated by a dot
 *
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("Unused")
@Serializable(with = ObjectIdentifierSerializer::class)
class ObjectIdentifier private constructor(private val parts: List<Int>) : ASN1Element {
    constructor(value: String) : this(value.split(".").map { it.toInt() })

    override fun write(sink: Sink) {
        sink.writeByte(tag.value)

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

        buffer.use {
            sink.writeASN1Length(it.size)
            sink.write(it, it.size)
        }
    }

    override fun equals(other: Any?): Boolean = when(other) {
        is ObjectIdentifier -> parts == other.parts
        else -> false
    }

    override fun asCollection(): ASN1Collection<*> =
        throw UnsupportedOperationException("Unable to convert object identifier to collection")
    override fun asString(): String =
        throw UnsupportedOperationException("Unable to convert object identifier to string")
    override fun asAny(): Any = this
    override fun toString(): String = parts.joinToString(".")
    override fun hashCode(): Int = parts.hashCode()

    companion object : ASN1Element.Factory<ObjectIdentifier> {
        override val tag: ASN1Element.ASN1Tag = ASN1Element.ASN1Tag.OBJECT_ID

        @JvmStatic
        override fun fromData(source: Source, length: Long): Result<ObjectIdentifier> {
            val firstByte = source.readByte()
            val parts = mutableListOf(firstByte / 40, firstByte % 40)
            var remaining = length - 1
            while (remaining > 0) {
                var value = 0
                var byte: Int

                do {
                    byte = source.readByte().toInt() and 0xFF
                    value = (value shl 7) or (byte and 0x7F)
                    remaining--
                } while (byte and 0x80 != 0)
                parts.add(value)
            }
            return Result.success(ObjectIdentifier(parts))
        }
    }
}
