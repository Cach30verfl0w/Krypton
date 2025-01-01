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

package de.cacheoverflow.krypton.asn1

import de.cacheoverflow.krypton.asn1.annotation.ClassKind
import de.cacheoverflow.krypton.asn1.annotation.StringKind
import de.cacheoverflow.krypton.asn1.annotation.WrappedInto
import de.cacheoverflow.krypton.asn1.serialization.ASN1Encoder
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.Serializable
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class ASN1EncoderTests : ShouldSpec() {
    init {
        @Serializable
        data class TestStructure(
            val value: Int?,
            val identifier: ObjectIdentifier,
            @StringKind(ASN1Utf8String::class) val value1: String,
            @StringKind(ASN1PrintableString::class) val value2: String
        )

        should("Kotlin class into ASN1 with null") {
            val structure = TestStructure(
                value = null,
                identifier = ObjectIdentifier("1.2.840.113549.1.1.1"),
                value1 = "Test1",
                value2 = "Test2"
            )

            assertEquals(
                expected = "301b050006092a864886f70d0101010c05546573743113055465737432",
                actual = ASN1Encoder.serialize(structure, TestStructure.serializer()).toHexString(),
                message = "Unable to encode ASN.1 sequence"
            )
        }
        should("Kotlin class into ASN1 without null") {
            val structure = TestStructure(
                value = 0x1,
                identifier = ObjectIdentifier("1.2.840.113549.1.1.1"),
                value1 = "Test1",
                value2 = "Test2"
            )

            assertEquals(
                expected = "301c02010106092a864886f70d0101010c05546573743113055465737432",
                actual = ASN1Encoder.serialize(structure, TestStructure.serializer()).toHexString(),
                message = "Unable to encode ASN.1 sequence"
            )
        }
        should("Kotlin class as set with sub-class into ASN1") {
            @Serializable
            @ClassKind(ASN1Set::class)
            data class TestStructure1(
                val value: Int?,
            )

            assertEquals(
                expected = "3003020101",
                actual = ASN1Encoder.serialize(
                    value = TestStructure1(0x1),
                    serializationStrategy = TestStructure1.serializer()
                ).toHexString(),
                message = "Unable to encode ASN.1 set"
            )
        }
        should("Kotlin class with sub-class into ASN1") {
            @Serializable
            data class TestStructure1(
                val value: Int?,
                val structure: TestStructure
            )

            assertEquals(
                expected = "3021020101301c02010106092a864886f70d0101010c05546573743113055465737432",
                actual = ASN1Encoder.serialize(
                    value = TestStructure1(
                        value = 0x1,
                        structure = TestStructure(
                            value = 0x1,
                            identifier = ObjectIdentifier("1.2.840.113549.1.1.1"),
                            value1 = "Test1",
                            value2 = "Test2"
                        )
                    ),
                    serializationStrategy = TestStructure1.serializer()
                ).toHexString(),
                message = "Unable to encode ASN.1 sequence containing another ASN.1 sequence"
            )
        }
        should("Kotlin class as sequence wrapped into another sequence") {
            run {
                @Serializable
                @WrappedInto(ASN1Sequence::class)
                data class WrappedSequence(val value: Int)
                assertEquals(
                    expected = "30053003020101",
                    actual = ASN1Encoder.serialize(WrappedSequence(0x1), WrappedSequence.serializer()).toHexString(),
                    message = "Unable to encode ASN.1 sequence containing another ASN.1 sequence"
                )
            }
            run {
                @Serializable
                @WrappedInto(ASN1OctetString::class)
                data class WrappedSequence(val value: Int)
                assertEquals(
                    expected = "04053003020101",
                    actual = ASN1Encoder.serialize(WrappedSequence(0x1), WrappedSequence.serializer()).toHexString(),
                    message = "Unable to encode ASN.1 octet string containing another ASN.1 sequence"
                )
            }
            run {
                @Serializable
                @WrappedInto(ASN1BitString::class)
                data class WrappedSequence(val value: Int)
                assertEquals(
                    expected = "0306003003020101",
                    actual = ASN1Encoder.serialize(WrappedSequence(0x1), WrappedSequence.serializer()).toHexString(),
                    message = "Unable to encode ASN.1 bitstring containing another ASN.1 sequence"
                )
            }
        }
    }
}
