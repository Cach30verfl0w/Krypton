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
 * @since  29/12/2024
 */
class ASN1BitString(private val paddingBits: Byte, private val bitData: ByteArray) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag.value)
        sink.writeASN1Length((bitData.size + 1).toLong())
        sink.writeByte(paddingBits)
        sink.write(bitData)
    }

    // TODO: Implement bitset logic for bit string

    override fun asCollection(): ASN1Collection<*> =
        throw UnsupportedOperationException("Unable to convert bit string to collection")
    override fun asString(): String =
        throw UnsupportedOperationException("Unable to convert bit string to string")
    override fun asAny(): Any = // TODO: Return bitset
        throw UnsupportedOperationException("Return BitSet")

    companion object : ASN1Element.Factory<ASN1BitString> {
        override val tag: ASN1Element.ASN1Tag = ASN1Element.ASN1Tag.BIT_STRING

        override fun fromData(source: Source, length: Long): Result<ASN1BitString> =
            Result.success(ASN1BitString(source.readByte(), source.readByteArray(length.toInt() - 1)))
    }
}
