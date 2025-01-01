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

package de.cacheoverflow.krypton.asn1.annotation

import de.cacheoverflow.krypton.asn1.ASN1String
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

/**
 * @author Cedric Hammes
 * @since  01/01/2024
 */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class StringKind(val value: KClass<out ASN1String<*>>)
