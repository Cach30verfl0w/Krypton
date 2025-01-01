/*
 * Copyright 2025 Cedric Hammes
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
import de.cacheoverflow.krypton.asn1.annotation.ClassKind
import de.cacheoverflow.krypton.asn1.annotation.WrappedInto
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readByteArray
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind

import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.TaggedEncoder
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * @author Cedric Hammes
 * @since  01/01/2025
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class ASN1Encoder private constructor(
    private val parent: ASN1Collection<*>?,
    type: KClass<out ASN1Collection<*>> = ASN1Sequence::class
) : TaggedEncoder<SerialASN1Tag>() {
    internal val sequence: ASN1Collection<*> = when(type) {
        ASN1Sequence::class -> ASN1Sequence()
        ASN1Set::class -> ASN1Set()
        else -> throw IllegalArgumentException("$type is not supported")
    }

    // @formatter:off
    override fun SerialDescriptor.getTag(index: Int): SerialASN1Tag = SerialASN1Tag.fromDescriptor(this, index)
    override fun encodeTaggedNull(tag: SerialASN1Tag) { sequence += ASN1Null }
    override fun encodeTaggedByte(tag: SerialASN1Tag, value: Byte) { sequence += ASN1Integer(value) }
    override fun encodeTaggedShort(tag: SerialASN1Tag, value: Short) { sequence += ASN1Integer(value) }
    override fun encodeTaggedInt(tag: SerialASN1Tag, value: Int) { sequence += ASN1Integer(value) }
    override fun encodeTaggedLong(tag: SerialASN1Tag, value: Long) { sequence += ASN1Integer(value) }
    override fun encodeTaggedString(tag: SerialASN1Tag, value: String) {
        sequence += when(val kind = tag.stringKind) {
            ASN1Utf8String::class -> ASN1Utf8String(value)
            ASN1PrintableString::class -> ASN1PrintableString(value)
            ASN1T61String::class -> ASN1T61String(value)
            ASN1IA5String::class -> ASN1IA5String(value)
            else -> throw IllegalArgumentException("Unable to deserialize string (type ${kind?.qualifiedName?: "unknown"} not supported)")
        }
    }
    // @formatter:on

    override fun endEncode(descriptor: SerialDescriptor) {
        parent?.add(sequence)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        fun beginStructure0(): CompositeEncoder = when (descriptor.kind) {
            StructureKind.CLASS -> ASN1Encoder(sequence, descriptor.findAnnotation<ClassKind>()?.value ?: ASN1Sequence::class)
            StructureKind.LIST -> ASN1Encoder(sequence) // TODO: Replace with specialized decoder
            else -> super.beginStructure(descriptor)
        }

        return when (val containerType = descriptor.findAnnotation<WrappedInto>()?.value) {
            null -> beginStructure0()
            else -> TODO("Structure wrapped into containerType")
        }
    }

    companion object {
        @JvmStatic
        fun <T> serialize(sink: Sink, value: T, serializationStrategy: SerializationStrategy<T>) =
            ASN1Encoder(null).also { it.encodeSerializableValue(serializationStrategy, value) }.sequence[0].write(sink)
        fun <T> serialize(value: T, serializationStrategy: SerializationStrategy<T>): ByteArray =
            Buffer().also { serialize(it, value, serializationStrategy) }.use { it.readByteArray() }
    }
}
