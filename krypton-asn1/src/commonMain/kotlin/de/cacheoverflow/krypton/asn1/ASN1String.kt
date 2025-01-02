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
import kotlinx.io.readByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class ASN1StringSerializer : KSerializer<ASN1String<*>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(requireNotNull(ASN1Element::class.qualifiedName), PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ASN1String<*> = when (decoder) {
        is ASN1Decoder -> decoder.root.dropFirstOrThrow()
        else -> TODO("Not implemented yet")
    }

    override fun serialize(encoder: Encoder, value: ASN1String<*>) = when (encoder) {
        is ASN1Encoder -> encoder.sequence += value
        else -> TODO("Not implemented yet")
    }
}

/**
 * This type is the abstract API for a simpler implementation of strings into the ASN.1 syntax. This "API" is created because of the
 * different string types supported by ASN.1 and it's encodings.
 *
 * @param value   The char sequence stored in the string
 * @param factory The factory for creating this string from binary sources
 *
 * @author Cedric Hammes
 * @since  30/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
@Serializable(with = ASN1StringSerializer::class)
sealed class ASN1String<T : ASN1String<T>>(
    private val factory: Factory<T>,
    var value: String
) : ASN1Element {
    init {
        require(factory.validator?.let { it(value) } ?: true) { "$value is not a valid string" }
    }

    override fun write(sink: Sink) {
        sink.writeByte(factory.tag.value)
        val binaryData = value.encodeToByteArray()
        sink.writeASN1Length(binaryData.size.toLong())
        sink.write(binaryData)
    }

    override fun asCollection(): ASN1Collection<*> =
        throw UnsupportedOperationException("Unable to convert string to collection")

    override fun asString(): String = value
    override fun asAny(): Any = asString()

    sealed class Factory<T : ASN1String<T>>(
        override val tag: ASN1Element.ASN1Tag,
        private val factory: (String) -> T,
        internal val validator: ((String) -> Boolean)? = null
    ) : ASN1Element.Factory<T> {
        override fun fromData(source: Source, length: Long): Result<T> =
            Result.runCatching { factory(source.readByteArray(length.toInt()).decodeToString()) }
    }
}

/**
 * This string is the implementation of a UTF-8 formatted string into the ASN.1 syntax. These can be sent over the network with the help of
 * the DER encoding.
 *
 * @param value The char sequence stored in the string
 *
 * @author Cedric Hammes
 * @since  30/12/2024
 *
 * @see [ASN.1 UTF8 string](https://www.oss.com/asn1/resources/asn1-made-simple/asn1-quick-reference/utf8string.html)
 */
class ASN1Utf8String(value: String) : ASN1String<ASN1Utf8String>(ASN1Utf8String, value) {
    companion object : Factory<ASN1Utf8String>(ASN1Element.ASN1Tag.UTF8_STRING, { ASN1Utf8String(it) })
}

/**
 * This string is the implementation of a string supporting the latter's A to Z (lowercase and uppercase), spaces, common punctuation marks
 * and digits. These can be sent over the network with the help of the DER encoding.
 *
 * @param value The char sequence stored in the string
 *
 * @author Cedric Hammes
 * @since  30/12/2024
 *
 * @see [ASN.1 printable string](https://www.oss.com/asn1/resources/asn1-made-simple/asn1-quick-reference/printablestring.html)
 */
class ASN1PrintableString(value: String) : ASN1String<ASN1PrintableString>(ASN1PrintableString, value) {
    companion object : Factory<ASN1PrintableString>(
        ASN1Element.ASN1Tag.PRINTABLE_STRING,
        { ASN1PrintableString(it) },
        { value -> value.all { it in ' '..'~' } }
    )
}

/**
 * This string is the implementation of a ISO 10656 formatted string into the ASN.1 syntax. These can be sent over the network with the help
 * of the DER encoding.
 *
 * @param value The char sequence stored in the string
 *
 * @author Cedric Hammes
 * @since  02/01/2025
 *
 * @see [ASN.1 universal string, OSS Nokalva](https://www.oss.com/asn1/resources/asn1-made-simple/asn1-quick-reference/universalstring.html)
 */
class ASN1UniversalString(value: String) : ASN1String<ASN1UniversalString>(ASN1UniversalString, value) {
    companion object : Factory<ASN1UniversalString>(
        ASN1Element.ASN1Tag.UNIVERSAL_STRING,
        { ASN1UniversalString(it) }
    )
}

/**
 * This string is the implementation of a string supporting unicode symbols into the ASN.1 syntax. These can be sent over the network with
 * the help of the DER encoding.
 *
 * @param value The char sequence stored in the string
 *
 * @author Cedric Hammes
 * @since  02/01/2025
 *
 * @see [ASN.1 BMPString, OSS Nokalva](https://www.oss.com/asn1/resources/asn1-made-simple/asn1-quick-reference/bmpstring.html)
 */
class ASN1BMPString(value: String) : ASN1String<ASN1BMPString>(ASN1BMPString, value) {
    companion object : Factory<ASN1BMPString>(
        ASN1Element.ASN1Tag.BMP_STRING,
        { ASN1BMPString(it) },
        { value -> value.all { it.code in 0x0000..0xFFFF && it.code !in 0xD800..0xDFFF } }
    )
}

/**
 * This string is the implementation of a string formatted as defined in the Recommendation T.61 for Teletex applications into the ASN.1
 * syntax. These can be sent over the network with the help of the DER encoding.
 *
 * @param value The char sequence stored in the string
 *
 * @author Cedric Hammes
 * @since  30/12/2024
 *
 * @see [ASN.1 teletex string, OSS Nokalva](https://www.oss.com/asn1/resources/asn1-made-simple/asn1-quick-reference/teletexstring.html)
 */
class ASN1T61String(value: String) : ASN1String<ASN1T61String>(ASN1T61String, value) {
    companion object : Factory<ASN1T61String>(
        ASN1Element.ASN1Tag.T61_STRING,
        { ASN1T61String(it) },
        { value -> value.all { it.code in 0..256 } }
    )
}

/**
 * This string is the implementation of a string supporting the ASCII alphabet into the ASN.1 syntax. These can be sent over the network
 * with the help of the DER encoding.
 *
 * @param value The char sequence stored in the string
 *
 * @author Cedric Hammes
 * @since  30/12/2024
 *
 * @see [ASN.1 IA5 string](https://www.oss.com/asn1/resources/asn1-made-simple/asn1-quick-reference/ia5string.html)
 */
class ASN1IA5String(value: String) : ASN1String<ASN1IA5String>(ASN1IA5String, value) {
    companion object : Factory<ASN1IA5String>(
        ASN1Element.ASN1Tag.IA5_STRING,
        { ASN1IA5String(it) },
        { value -> value.all { it.code in 0..127 } }
    )
}
