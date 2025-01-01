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
import de.cacheoverflow.krypton.asn1.ASN1IA5String.Companion.readASN1Length
import de.cacheoverflow.krypton.asn1.annotation.StringKind
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * @author Cedric Hammes
 * @since  31/12/2024
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class ASN1Decoder private constructor(internal val source: Source) : TaggedDecoder<ASN1Decoder.ElementTag>(), AutoCloseable {
    private var currentIndex: Int = 0
    override fun SerialDescriptor.getTag(index: Int): ElementTag = ElementTag.fromDescriptor(this, index)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (descriptor.elementsCount == currentIndex) CompositeDecoder.DECODE_DONE else currentIndex++

    override fun decodeTaggedByte(tag: ElementTag): Byte = ASN1Integer.fromSource(source).getOrThrow().asByte()
    override fun decodeTaggedShort(tag: ElementTag): Short = ASN1Integer.fromSource(source).getOrThrow().asShort()
    override fun decodeTaggedInt(tag: ElementTag): Int = ASN1Integer.fromSource(source).getOrThrow().asInt()
    override fun decodeTaggedLong(tag: ElementTag): Long = ASN1Integer.fromSource(source).getOrThrow().asLong()
    override fun decodeTaggedString(tag: ElementTag): String = when (val kind = tag.stringKind) {
        ASN1Utf8String::class -> ASN1Utf8String.fromSource(source)
        ASN1PrintableString::class -> ASN1PrintableString.fromSource(source)
        ASN1T61String::class -> ASN1T61String.fromSource(source)
        ASN1IA5String::class -> ASN1IA5String.fromSource(source)
        else -> throw IllegalArgumentException("Unable to deserialize string (type ${kind?.qualifiedName ?: "eaea"} not supported)")
    }.getOrThrow().asString()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val tag = ASN1Element.ASN1Tag(source)
        val length = source.readASN1Length()
        val kind = descriptor.kind
        return when {
            kind == StructureKind.CLASS && tag == ASN1Element.ASN1Tag.SEQUENCE -> ASN1Decoder(source) // TODO: Validate type of "collection"
            kind == StructureKind.LIST && tag.isList() -> ASN1Decoder(source) // TODO: Replace with specialized decoder
            else -> super.beginStructure(descriptor)
        }
    }

    override fun close() = source.close()

    /**
     * @author Cedric Hammes
     * @since  01/01/2024
     */
    class ElementTag private constructor(internal val stringKind: KClass<out ASN1String<*>>?) {
        companion object {
            private inline fun <reified T : Annotation> SerialDescriptor.findAnnotation(index: Int): T? =
                getElementAnnotations(index).firstOrNull { it is T } as T?

            internal fun fromDescriptor(descriptor: SerialDescriptor, index: Int): ElementTag =
                ElementTag(descriptor.findAnnotation<StringKind>(index)?.value)
        }
    }

    companion object {
        @JvmStatic
        fun <T> deserialize(source: Source, deserializationStrategy: DeserializationStrategy<T>): T =
            ASN1Decoder(source).decodeSerializableValue(deserializationStrategy)
        @JvmStatic
        fun <T> deserialize(source: ByteArray, deserializationStrategy: DeserializationStrategy<T>): T =
            Buffer().also { it.write(source) }.use { deserialize(it, deserializationStrategy) }
    }
}