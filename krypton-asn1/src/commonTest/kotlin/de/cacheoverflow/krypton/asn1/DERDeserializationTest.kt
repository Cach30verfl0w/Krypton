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

import de.cacheoverflow.krypton.asn1.annotation.StringKind
import de.cacheoverflow.krypton.asn1.serialization.ASN1Decoder
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.io.Buffer
import kotlinx.serialization.Serializable
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class DERDeserializationTest : ShouldSpec() {
    init {
        should("deserialize hex string into kotlin class") {
            @Serializable
            data class TestStructure(
                val value: Int,
                val identifier: ObjectIdentifier,
                @StringKind(ASN1Utf8String::class) val value1: String,
                @StringKind(ASN1PrintableString::class) val value2: String
            )

            assertEquals(
                expected = TestStructure(
                    value = 0x1,
                    identifier = ObjectIdentifier("1.2.840.113549.1.1.1"),
                    value1 = "Test1",
                    value2 = "Test2"
                ),
                actual = Buffer()
                    .also { it.write("301c02010106092a864886f70d0101010c05546573743113055465737432".hexToByteArray()) }
                    .use { ASN1Decoder.deserialize(it, TestStructure.serializer()) },
                message = "Unable to deserialize ASN.1 sequence"
            )
        }
    }
}
