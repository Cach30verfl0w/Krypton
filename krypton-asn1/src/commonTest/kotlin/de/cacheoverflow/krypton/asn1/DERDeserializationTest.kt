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

import de.cacheoverflow.krypton.asn1.element.*
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
class DERDeserializationTest : ShouldSpec() {
    init {
        should("read and re-write encoded ASN1") {
            SystemFileSystem.source(Path("src/commonTest/resources/keystore.jks")).buffered().use { source ->
                val readData = source.peek().use { it.readByteArray() }
                val sequence = ASN1ParserContext.default().readObject(readData).getOrThrow()
                val wroteData = Buffer().also { sequence.write(it) }.use { it.readByteArray() }

                println("Read:  ${readData.toHexString()}")
                println("Wrote: ${wroteData.toHexString()}")
                println()
                writeStructure(0, sequence)
                println()
                assertTrue(wroteData.contentEquals(readData), "Unable to replicate data by re-writing read data")
            }
        }
        should("deserialize X509 certificate") {
            SystemFileSystem.source(Path("src/commonTest/resources/certificate.pem")).buffered().use { source ->
                val lines = source.readByteArray().decodeToString().lines()
                val data = Base64.decode(lines.subList(1, lines.size - 2).joinToString(""))
                val sequence = ASN1ParserContext.default().readObject(data).getOrThrow()
                val wroteData = Buffer().also { sequence.write(it) }.use { it.readByteArray() }

                println("Read:  ${data.toHexString()}")
                println("Wrote: ${wroteData.toHexString()}")
                println()
                writeStructure(0, sequence)
                println()
                println(data.toHexString() == wroteData.toHexString())
                assertTrue(wroteData.contentEquals(data), "Unable to replicate data by re-writing read data")
            }
        }
    }

    private fun writeStructure(level: Int, element: ASN1Element): Unit = when(element) {
        is ASN1Sequence -> {
            println("${"\t".repeat(level)} Sequence (${element.children.size}):")
            for (child in element.children) {
                writeStructure(level + 1, child)
            }
        }
        is ASN1Set -> {
            println("${"\t".repeat(level)} Set (${element.children.size}):")
            for (child in element.children) {
                writeStructure(level + 1, child)
            }
        }
        is ASN1ContextSpecificElement -> {
            println("${"\t".repeat(level)} Context-specific:")
            writeStructure(level + 1, element.element)
        }
        else -> println("${"\t".repeat(level)} $element")
    }
}
