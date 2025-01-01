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

import de.cacheoverflow.krypton.asn1.ASN1String
import de.cacheoverflow.krypton.asn1.annotation.StringKind
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T : Annotation> SerialDescriptor.findAnnotation(): T? = this.annotations.firstOrNull { it is T } as T?

/**
 * @author Cedric Hammes
 * @since  01/01/2024
 */
class SerialASN1Tag private constructor(internal val stringKind: KClass<out ASN1String<*>>?) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private inline fun <reified T : Annotation> SerialDescriptor.findAnnotation(index: Int): T? =
            getElementAnnotations(index).firstOrNull { it is T } as T?

        internal fun fromDescriptor(descriptor: SerialDescriptor, index: Int): SerialASN1Tag =
            SerialASN1Tag(descriptor.findAnnotation<StringKind>(index)?.value)
    }
}
