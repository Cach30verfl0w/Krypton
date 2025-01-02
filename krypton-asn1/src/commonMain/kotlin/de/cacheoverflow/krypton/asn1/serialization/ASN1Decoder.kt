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

package de.cacheoverflow.krypton.asn1.serialization

import de.cacheoverflow.krypton.asn1.*
import de.cacheoverflow.krypton.asn1.annotation.ClassKind
import de.cacheoverflow.krypton.asn1.annotation.ExperimentalAsn1API
import de.cacheoverflow.krypton.asn1.annotation.WrappedInto
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlin.jvm.JvmStatic

internal inline fun <reified T : ASN1Element> ASN1Collection<*>.dropFirstOrThrow(): T {
    val element = requireNotNull(removeFirstOrNull()) { "Expected element, but got null" }
    return element as? T
        ?: throw IllegalArgumentException("Expected ${T::class.qualifiedName}, but got ${element::class.qualifiedName}")
}

/**
 * @author Cedric Hammes
 * @since  31/12/2024
 */
@ExperimentalAsn1API
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class ASN1Decoder private constructor(internal val root: ASN1Collection<*>, private val initial: Boolean) : TaggedDecoder<SerialASN1Tag>() {
    private var currentIndex: Int = 0
    override fun SerialDescriptor.getTag(index: Int): SerialASN1Tag = SerialASN1Tag.fromDescriptor(this, index)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (descriptor.elementsCount == currentIndex) CompositeDecoder.DECODE_DONE else currentIndex++

    override fun decodeTaggedByte(tag: SerialASN1Tag): Byte = root.dropFirstOrThrow<ASN1Integer>().asByte()
    override fun decodeTaggedShort(tag: SerialASN1Tag): Short = root.dropFirstOrThrow<ASN1Integer>().asShort()
    override fun decodeTaggedInt(tag: SerialASN1Tag): Int = root.dropFirstOrThrow<ASN1Integer>().asInt()
    override fun decodeTaggedLong(tag: SerialASN1Tag): Long = root.dropFirstOrThrow<ASN1Integer>().asLong()
    override fun decodeTaggedString(tag: SerialASN1Tag): String {
        val kind = requireNotNull(tag.stringKind) { "No string kind found, please specify with @ClassKind" }
        val element = root.dropFirstOrThrow<ASN1String<*>>()
        if (element::class != kind)
            throw IllegalArgumentException("Expected '${kind::qualifiedName}', but got '${element::class.qualifiedName}'")
        return element.asString()
    }

    override fun decodeNotNullMark(): Boolean = if (root.first() is ASN1Null) {
        root.removeFirst()
        false
    } else true

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        fun beginStructure0(element: ASN1Element): CompositeDecoder {
            return when(descriptor.kind) { // TODO: Add support for list
                StructureKind.CLASS -> {
                    val classKind = descriptor.findAnnotation<ClassKind>()?.value ?: ASN1Sequence::class
                    if (element::class != classKind)
                        throw IllegalArgumentException("Expected '${classKind::qualifiedName}', but got ${element::class.qualifiedName}")
                    return ASN1Decoder(element as ASN1Collection<*>, false)
                }
                else -> super.beginStructure(descriptor)
            }
        }

        return when (val containerType = descriptor.findAnnotation<WrappedInto>()?.value) {
            null -> beginStructure0(if (initial) root else root.dropFirstOrThrow<ASN1Collection<*>>())
            else -> {
                val element = requireNotNull(root.removeFirst())
                if (element::class != containerType)
                    throw IllegalArgumentException("Expected ${containerType.qualifiedName}, but got ${element::class.qualifiedName}")
                beginStructure0((element as ASN1ElementContainer).unwrap())
            }
        }
    }

    companion object {
        @JvmStatic
        fun <T> deserialize(source: ASN1Collection<*>, deserializationStrategy: DeserializationStrategy<T>): T =
            ASN1Decoder(source, true).decodeSerializableValue(deserializationStrategy)

        @JvmStatic
        fun <T> deserialize(source: Source, deserializationStrategy: DeserializationStrategy<T>): Result<T> {
            val element = ASN1Element.ASN1Tag.read(source)
            if (element.isFailure) return Result.failure(requireNotNull(element.exceptionOrNull()))
            if (element.getOrThrow() !is ASN1Collection<*>) return Result.failure(IllegalArgumentException("Element is not collection"))
            return Result.success(deserialize(element.getOrThrow() as ASN1Collection<*>, deserializationStrategy))
        }

        @JvmStatic
        fun <T> deserialize(source: ByteArray, deserializationStrategy: DeserializationStrategy<T>): Result<T> =
            deserialize(Buffer().also { it.write(source) }, deserializationStrategy)
    }
}
