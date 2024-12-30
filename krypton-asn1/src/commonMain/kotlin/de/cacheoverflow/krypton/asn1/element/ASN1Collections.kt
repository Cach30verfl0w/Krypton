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
 * @since  30/12/2024
 */
sealed class ASN1Collection<T : ASN1Collection<T>>(private val factory: Factory<T>, val children: List<ASN1Element>) : ASN1Element {
    override fun write(sink: Sink) {
        sink.writeByte(factory.tag)
        Buffer().also { buffer -> children.forEach { it.write(buffer) } }.use { buffer ->
            sink.writeASN1Length(buffer.size)
            sink.write(buffer, buffer.size)
        }
    }

    sealed class Factory<T : ASN1Collection<T>>(
        override val tagType: Byte,
        private val factory: (List<ASN1Element>) -> T
    ) : ASN1ElementFactory<T> {
        override val tagClass: EnumTagClass = EnumTagClass.UNIVERSAL
        override val isConstructed: Boolean = true

        override fun fromData(context: ASN1ParserContext, elementData: Buffer): Result<T> {
            val children = mutableListOf<ASN1Element>()
            while (elementData.size > 0L) {
                val result = context.readObject(elementData)
                when {
                    result.isFailure -> return Result.failure(requireNotNull(result.exceptionOrNull()))
                    else -> children.add(result.getOrThrow())
                }
            }
            return Result.success(factory(children))
        }
    }
}

class ASN1Sequence(children: List<ASN1Element>) : ASN1Collection<ASN1Sequence>(ASN1Sequence, children) {
    companion object : Factory<ASN1Sequence>(0x10, { ASN1Sequence(it) })
}

class ASN1Set(children: List<ASN1Element>) : ASN1Collection<ASN1Set>(ASN1Set, children) {
    companion object : Factory<ASN1Set>(0x11, { ASN1Set(it) })
}
