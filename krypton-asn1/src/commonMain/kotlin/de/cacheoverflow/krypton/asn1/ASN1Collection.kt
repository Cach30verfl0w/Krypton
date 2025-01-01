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

import kotlinx.io.*
import kotlin.jvm.JvmStatic

/**
 * @author Cedric Hammes
 * @since  30/12/2024
 */
@Suppress("MemberVisibilityCanBePrivate")
sealed class ASN1Collection<T : ASN1Collection<T>>(
    private val factory: ASN1Element.Factory<T>,
    val children: MutableList<ASN1Element>
) : ASN1Element, ASN1ElementContainer, MutableList<ASN1Element> by children {
    override fun write(sink: Sink) {
        sink.writeByte(factory.tag.value)
        Buffer().also { buffer -> children.forEach { it.write(buffer) } }.use { buffer ->
            sink.writeASN1Length(buffer.size)
            sink.write(buffer, buffer.size)
        }
    }

    override fun toString(): String = "Collection(${children.joinToString(", ")})"
    override fun unwrap(): ASN1Element = children[0]
    override fun asCollection(): ASN1Collection<*> = this
    override fun asString(): String = throw UnsupportedOperationException("Unable to convert collection to string")
    override fun asAny(): Any = children

    sealed class Factory<T : ASN1Collection<T>>(
        override val tag: ASN1Element.ASN1Tag,
        private val createCollection: (MutableList<ASN1Element>) -> T
    ) : ASN1Element.Factory<T> {
        override fun fromData(source: Source, length: Long): Result<T> {
            val children = mutableListOf<ASN1Element>()
            // TODO: Remove buffer creation
            Buffer().also { it.write(source.readByteArray(length.toInt())) }.use { sourceBuffer ->
                while (sourceBuffer.size > 0) {
                    val tag = ASN1Element.ASN1Tag(sourceBuffer)
                    val childLength = sourceBuffer.readASN1Length()
                    val element = (tag.findFactory() ?: return Result.failure(IllegalArgumentException("Invalid tag $tag")))
                        .fromData(sourceBuffer, childLength)
                    if (element.isFailure) return Result.failure(requireNotNull(element.exceptionOrNull()))
                    children.add(element.getOrThrow())
                }
            }
            return Result.success(createCollection(children))
        }

        override fun wrap(value: ASN1Element): T = createCollection(mutableListOf(value))
    }
}

/**
 * @author Cedric Hammes
 * @since  30/12/2024
 */
class ASN1Sequence(children: MutableList<ASN1Element>) : ASN1Collection<ASN1Sequence>(ASN1Sequence, children) {
    companion object : Factory<ASN1Sequence>(ASN1Element.ASN1Tag.SEQUENCE, { ASN1Sequence(it) }) {
        @JvmStatic
        fun of(vararg children: ASN1Element): ASN1Sequence = ASN1Sequence(children.toMutableList())
    }
}

/**
 * @author Cedric Hammes
 * @since  30/12/2024
 */
class ASN1Set(children: MutableList<ASN1Element>) : ASN1Collection<ASN1Set>(ASN1Set, children) {
    companion object : Factory<ASN1Set>(ASN1Element.ASN1Tag.SET, { ASN1Set(it) }) {
        @JvmStatic
        fun of(vararg children: ASN1Element): ASN1Sequence = ASN1Sequence(children.toMutableList())
    }
}
