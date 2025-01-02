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
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
internal class ASN1NullSerializer : KSerializer<ASN1Null> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("null", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ASN1Null) = when(encoder) {
        is ASN1Encoder -> encoder.sequence += value
        else -> encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): ASN1Null = when(decoder) {
        is ASN1Decoder -> decoder.root.dropFirstOrThrow()
        else -> decoder.decodeNotNullMark().let { ASN1Null }
    }

}

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Serializable(with = ASN1NullSerializer::class)
object ASN1Null : ASN1Element, ASN1Element.Factory<ASN1Null> {
    override val tag: ASN1Element.ASN1Tag = ASN1Element.ASN1Tag.NULL
    override fun fromData(source: Source, length: Long): Result<ASN1Null> = Result.success(ASN1Null)

    override fun toString(): String = "null"
    override fun asCollection(): ASN1Collection<*> =
        throw UnsupportedOperationException("Unable to convert null to collection")
    override fun asString(): String =
        throw UnsupportedOperationException("Unable to convert null to string")
    override fun asAny(): Any =
        throw UnsupportedOperationException("Unable to convert null to object")

    override fun write(sink: Sink) {
        sink.writeByte(tag.value)
        sink.writeASN1Length(0L)
    }
}
