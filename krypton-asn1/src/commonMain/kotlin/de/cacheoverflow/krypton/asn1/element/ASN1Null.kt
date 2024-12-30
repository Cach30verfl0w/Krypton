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
import kotlinx.io.Buffer
import kotlinx.io.Sink

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
object ASN1Null : ASN1Element, ASN1ElementFactory<ASN1Null> {
    override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
    override val tagType: Byte = 0x05
    override val isConstructed: Boolean = false

    override fun fromData(context: ASN1ParserContext, elementData: Buffer): Result<ASN1Null> = Result.success(ASN1Null)
    override fun write(sink: Sink) {
        sink.writeByte(tag)
        sink.writeASN1Length(0L)
    }

    override fun toString(): String = "Null"
}
