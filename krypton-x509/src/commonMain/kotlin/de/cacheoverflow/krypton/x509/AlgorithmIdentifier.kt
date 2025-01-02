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

package de.cacheoverflow.krypton.x509

import de.cacheoverflow.krypton.asn1.ASN1Null
import de.cacheoverflow.krypton.asn1.ASN1Sequence
import de.cacheoverflow.krypton.asn1.ObjectIdentifier
import de.cacheoverflow.krypton.asn1.annotation.ClassKind
import kotlinx.serialization.Serializable

/**
 * TODO: Implementation of a parameters
 *
 * @author Cedric Hammes
 * @since  02/01/2024
 */
@Serializable
@ClassKind(ASN1Sequence::class)
data class AlgorithmIdentifier(val algorithm: ObjectIdentifier, val parameters: ASN1Null)
