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

import de.cacheoverflow.krypton.asn1.annotation.ClassKind
import de.cacheoverflow.krypton.asn1.annotation.StringKind
import de.cacheoverflow.krypton.asn1.annotation.WrappedInto
import de.cacheoverflow.krypton.asn1.serialization.ASN1Decoder
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.Serializable
import kotlin.test.assertEquals

@Suppress("Unused")
@OptIn(ExperimentalStdlibApi::class)
class ASN1DecoderTests : ShouldSpec() {
    init {
        @Serializable
        data class TestStructure(
            val value: Int?,
            val identifier: ObjectIdentifier,
            @StringKind(ASN1Utf8String::class) val value1: String,
            @StringKind(ASN1PrintableString::class) val value2: String
        )

        should("Kotlin class from ASN1 with null") {
            assertEquals(
                expected = TestStructure(
                    value = null,
                    identifier = ObjectIdentifier("1.2.840.113549.1.1.1"),
                    value1 = "Test1",
                    value2 = "Test2"
                ),
                actual = ASN1Decoder.deserialize(
                    source = "301b050006092a864886f70d0101010c05546573743113055465737432".hexToByteArray(),
                    deserializationStrategy = TestStructure.serializer()
                ).getOrThrow(),
                message = "Unable to deserialize ASN.1 sequence"
            )
        }
        should("Kotlin class from ASN1 without null") {
            assertEquals(
                expected = TestStructure(
                    value = 0x1,
                    identifier = ObjectIdentifier("1.2.840.113549.1.1.1"),
                    value1 = "Test1",
                    value2 = "Test2"
                ),
                actual = ASN1Decoder.deserialize(
                    source = "301c02010106092a864886f70d0101010c05546573743113055465737432".hexToByteArray(),
                    deserializationStrategy = TestStructure.serializer()
                ).getOrThrow(),
                message = "Unable to deserialize ASN.1 sequence"
            )
        }
        should("Kotlin class as set with sub-class rom ASN1") {
            @Serializable
            @ClassKind(ASN1Set::class)
            data class TestStructure1(
                val value: Int?,
            )

            assertEquals(
                expected = TestStructure1(0x1),
                actual = ASN1Decoder.deserialize(
                    source = "3103020101".hexToByteArray(),
                    deserializationStrategy = TestStructure1.serializer()
                ).getOrThrow(),
                message = "Unable to encode ASN.1 set"
            )
        }
        should("Kotlin class with sub-class from ASN1") {
            @Serializable
            data class TestStructure1(
                val value: Int?,
                val structure: TestStructure
            )

            assertEquals(
                expected = TestStructure1(
                    value = 0x1,
                    structure = TestStructure(
                        value = 0x1,
                        identifier = ObjectIdentifier("1.2.840.113549.1.1.1"),
                        value1 = "Test1",
                        value2 = "Test2"
                    )
                ),
                actual = ASN1Decoder.deserialize(
                    source = "3021020101301c02010106092a864886f70d0101010c05546573743113055465737432".hexToByteArray(),
                    deserializationStrategy = TestStructure1.serializer()
                ).getOrThrow(),
                message = "Unable to decode ASN.1 sequence containing another ASN.1 sequence"
            )
        }
        should("Kotlin class as sequence wrapped in another sequence") {
            run {
                @Serializable
                @WrappedInto(ASN1Sequence::class)
                data class WrappedSequence(val value: Int)

                @Serializable
                data class Sequence(val value: Int, val sequence: WrappedSequence)

                assertEquals(
                    expected = Sequence(0x1, WrappedSequence(0x1)),
                    actual = ASN1Decoder.deserialize(
                        source = "300a02010130053003020101".hexToByteArray(),
                        deserializationStrategy = Sequence.serializer()
                    ).getOrThrow(),
                    message = "Unable to decode ASN.1 sequence containing another ASN.1 sequence"
                )
            }
            /*run {
                @Serializable
                @WrappedInto(ASN1OctetString::class)
                data class WrappedSequence(val value: Int)

                assertEquals(
                    expected = WrappedSequence(0x1),
                    actual = ASN1Decoder.deserialize(
                        source = "30053003020101".hexToByteArray(), // TODO: Another source sequence
                        deserializationStrategy = WrappedSequence.serializer()
                    ).getOrThrow(),
                    message = "Unable to decode ASN.1 octet string containing another ASN.1 sequence"
                )
            }
            run {
                @Serializable
                @WrappedInto(ASN1BitString::class)
                data class WrappedSequence(val value: Int)

                assertEquals(
                    expected = WrappedSequence(0x1),
                    actual = ASN1Decoder.deserialize(
                        source = "30053003020101".hexToByteArray(), // TODO: Another source sequence
                        deserializationStrategy = WrappedSequence.serializer()
                    ).getOrThrow(),
                    message = "Unable to decode ASN.1 bitstring containing another ASN.1 sequence"
                )
            }*/
        }
    }
}
