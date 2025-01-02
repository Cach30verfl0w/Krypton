import de.cacheoverflow.krypton.asn1.ObjectIdentifier
import de.cacheoverflow.krypton.asn1.annotation.ExperimentalAsn1API
import de.cacheoverflow.krypton.asn1.serialization.ASN1Decoder
import de.cacheoverflow.krypton.x509.PrivateKeyInfo
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals

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

@OptIn(ExperimentalEncodingApi::class, ExperimentalAsn1API::class)
class PrivateKeyInfoTests : ShouldSpec() {
    init {
        should("deserialize private key info from file") {
            SystemFileSystem.source(Path("src/commonTest/resources/private-key.pem")).buffered().use { source ->
                val lines = source.readByteArray().decodeToString().split("\n")
                val rawData = Base64.decode(lines.drop(1).dropLast(2).joinToString(""))
                val privateKeyInfo = ASN1Decoder.deserialize(rawData, PrivateKeyInfo.serializer()).getOrThrow()
                println(privateKeyInfo)
                assertEquals(ObjectIdentifier("1.2.840.113549.1.1.1"), privateKeyInfo.algorithmIdentifier.algorithm)
            }
        }
    }
}
