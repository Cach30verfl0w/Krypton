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

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

/**
 * @author Cedric Hammes
 * @since  30/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
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
 * @author Cedric Hammes
 * @since  30/12/2024
 */
class ASN1Utf8String(value: String) : ASN1String<ASN1Utf8String>(ASN1Utf8String, value) {
    companion object : Factory<ASN1Utf8String>(ASN1Element.ASN1Tag.UTF8_STRING, { ASN1Utf8String(it) })
}

/**
 * @author Cedric Hammes
 * @since  30/12/2024
 */
class ASN1PrintableString(value: String) : ASN1String<ASN1PrintableString>(ASN1PrintableString, value) {
    companion object : Factory<ASN1PrintableString>(
        ASN1Element.ASN1Tag.PRINTABLE_STRING,
        { ASN1PrintableString(it) },
        { value -> value.all { it in ' '..'~' } }
    )
}

/**
 * @author Cedric Hammes
 * @since  30/12/2024
 */
class ASN1T61String(value: String) : ASN1String<ASN1T61String>(ASN1T61String, value) {
    companion object : Factory<ASN1T61String>(
        ASN1Element.ASN1Tag.T61_STRING,
        { ASN1T61String(it) },
        { value -> value.all { it.code in 0..256 } }
    )
}

/**
 * @author Cedric Hammes
 * @since  30/12/2024
 */
class ASN1IA5String(value: String) : ASN1String<ASN1IA5String>(ASN1IA5String, value) {
    companion object : Factory<ASN1IA5String>(
        ASN1Element.ASN1Tag.IA5_STRING,
        { ASN1IA5String(it) },
        { value -> value.all { it.code in 0..127 } }
    )
}
