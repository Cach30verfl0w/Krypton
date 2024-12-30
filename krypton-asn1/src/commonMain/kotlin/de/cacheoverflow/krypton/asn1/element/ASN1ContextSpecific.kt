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

import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlin.jvm.JvmStatic

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
class ASN1ContextSpecific private constructor(var tag: Byte, var element: ASN1Element) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(tag)
        Buffer().also { element.write(it) }.use { buffer ->
            sink.writeASN1Length(buffer.size)
            sink.write(buffer, buffer.size)
        }
    }

    companion object {
        @JvmStatic
        fun fromData(context: ASN1ParserContext, tag: Byte, data: Buffer): Result<ASN1ContextSpecific> =
            context.readObject(data).map { ASN1ContextSpecific(tag, it) }
    }
}
