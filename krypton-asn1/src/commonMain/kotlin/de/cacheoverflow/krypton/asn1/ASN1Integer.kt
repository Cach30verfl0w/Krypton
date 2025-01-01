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

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate", "Unused")
class ASN1Integer(var value: BigInteger) : ASN1Element {
    constructor(value: Byte) : this(BigInteger.fromByte(value))
    constructor(value: Short) : this(BigInteger.fromShort(value))
    constructor(value: Int) : this(BigInteger.fromInt(value))
    constructor(value: Long) : this(BigInteger.fromLong(value))

    override fun toString(): String = value.toString()
    override fun asCollection(): ASN1Collection<*> =
        throw UnsupportedOperationException("Unable to convert integer to collection")
    override fun asString(): String =
        throw UnsupportedOperationException("Unable to convert integer to string")
    override fun asAny(): Any = value

    override fun write(sink: Sink) {
        sink.writeByte(tag.value)
        val binaryData = value.toByteArray()
        sink.writeASN1Length(binaryData.size.toLong())
        sink.write(binaryData)
    }

    inline fun asByte(): Byte = value.byteValue()
    inline fun asShort(): Short = value.shortValue()
    inline fun asInt(): Int = value.intValue()
    inline fun asLong(): Long = value.longValue()

    companion object : ASN1Element.Factory<ASN1Integer> {
        override val tag: ASN1Element.ASN1Tag = ASN1Element.ASN1Tag.INTEGER
        override fun fromData(source: Source, length: Long): Result<ASN1Integer> = // TODO: Negative number support
            Result.success(ASN1Integer(BigInteger.fromByteArray(source.readByteArray(length.toInt()), Sign.POSITIVE)))
    }
}
