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

/**
 * This annotation is used to mark unfinished/WIP serialization or encoding/decoding API targeting the ASN.1 syntax's formats like BER or
 * DER.
 *
 * @author Cedric Hammes
 * @since  02/01/2024
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This ASN.1 serialization API is experimental. APIs annotated with this annotation can be incomplete"
)
annotation class ExperimentalAsn1API
