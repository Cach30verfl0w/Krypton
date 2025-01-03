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
import de.cacheoverflow.krypton.asn1.serialization.dropFirstOrThrow
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class ASN1ObjectStringSerializer : KSerializer<ASN1OctetString> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(requireNotNull(ASN1OctetString::class.qualifiedName), PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ASN1OctetString) = when (encoder) {
        is ASN1Encoder -> encoder.sequence += value
        else -> for (b in value.data) encoder.encodeByte(b)
    }

    override fun deserialize(decoder: Decoder): ASN1OctetString = when(decoder) {
        is ASN1Decoder -> decoder.root.dropFirstOrThrow()
        else -> TODO("Not implemented yet")
    }
}

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
@Serializable(with = ASN1ObjectStringSerializer::class)
class ASN1OctetString(var data: ByteArray) : ASN1Element, ASN1ElementContainer {
    override fun write(sink: Sink) {
        sink.writeByte(tag.value)
        sink.writeASN1Length(data.size.toLong())
        sink.write(data)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = data.toHexString()
    override fun unwrap(): ASN1Element = TODO("Deserialize element from source")
    override fun asCollection(): ASN1Collection<*> = // TODO: Try to parse into collection
        throw UnsupportedOperationException("Unable to convert octet string to collection")
    override fun asString(): String = data.decodeToString()
    override fun asAny(): Any = asString()

    companion object : ASN1Element.Factory<ASN1OctetString> {
        override val tag: ASN1Element.ASN1Tag = ASN1Element.ASN1Tag.OCTET_STRING
        override fun fromData(source: Source, length: Long): Result<ASN1OctetString> =
            Result.success(ASN1OctetString(source.readByteArray(length.toInt())))

        override fun wrap(value: ASN1Element): ASN1OctetString =
            ASN1OctetString(Buffer().also { value.write(it) }.use { it.readByteArray() })
    }
}
