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
@Suppress("MemberVisibilityCanBePrivate")
class ASN1OctetString(var data: ByteArray) : ASN1Element, ASN1ElementContainer {
    override fun write(sink: Sink) {
        sink.writeByte(tag.value)
        sink.writeASN1Length(data.size.toLong())
        sink.write(data)
    }

    override fun unwrap(): ASN1Element = TODO("Deserialize element from source")
    override fun asCollection(): ASN1Collection<*> = // TODO: Try to parse into collection
        throw UnsupportedOperationException("Unable to convert octet string to collection")
    override fun asString(): String = data.decodeToString()
    override fun asAny(): Any = asString()

    companion object : ASN1Element.Factory<ASN1OctetString> {
        override val tag: ASN1Element.ASN1Tag = ASN1Element.ASN1Tag.OCTET_STRING
        override fun fromData(source: Source, length: Long): Result<ASN1OctetString> =
            Result.success(ASN1OctetString(source.readByteArray(length.toInt())))
    }
}
