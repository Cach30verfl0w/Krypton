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

import de.cacheoverflow.krypton.asn1.element.ASN1ContextSpecificElement
import de.cacheoverflow.krypton.asn1.element.ASN1Element
import de.cacheoverflow.krypton.asn1.element.ASN1ParserContext
import de.cacheoverflow.krypton.asn1.element.ASN1Sequence
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class DERDeserializationTest : ShouldSpec() {
    init {
        should("eaeaea") {
            SystemFileSystem.source(Path("/home/cach30verfl0w/IdeaProjects/krypton/krypton-asn1/keystore.jks")).buffered().use { source ->
                val testObject = ASN1ParserContext.default().readObject(source)
                print(0, testObject)
            }
        }
    }
}

fun print(level: Int, element: ASN1Element) {
    if (element is ASN1Sequence) {
        println("\t".repeat(level) + " Sequence:")
        for (child in element.children) {
            print(level + 1, child)
        }
    } else if (element is ASN1ContextSpecificElement) {
        println("\t".repeat(level) + " Context-specific [0]:")
        print(level + 1, element.element)
    } else {
        println("\t".repeat(level) + " " + element)
    }
}