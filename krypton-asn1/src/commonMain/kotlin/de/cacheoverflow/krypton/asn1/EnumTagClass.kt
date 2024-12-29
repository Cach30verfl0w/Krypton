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

import kotlin.jvm.JvmStatic

/**
 * @author Cedric Hammes
 * @since  29/12/2024
 */
enum class EnumTagClass(private val literal: String, val value: Byte) {
    UNIVERSAL("Universal", 0x00),
    APPLICATION("Application", 0x01),
    CONTEXT_SPECIFIC("Context-Specific", 0x02),
    PRIVATE("Private", 0x03),
    UNKNOWN("Unknown", 0xF);

    override fun toString(): String = literal

    companion object {
        @JvmStatic
        fun byByte(value: Byte): EnumTagClass = EnumTagClass.entries.firstOrNull { it.value == value } ?: UNKNOWN
    }
}
