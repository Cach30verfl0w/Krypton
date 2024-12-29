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

package de.cacheoverflow.krypton.core

/**
 * @author Cedric Hammes
 * @since  29/12/2025
 */
expect class Key : AutoCloseable {
    val algorithm: String
    val usages: Array<Usage>
    val type: Type

    override fun close()

    /**
     * @author Cedric Hammes
     * @since  29/12/2025
     */
    enum class Usage {
        ENCRYPT,
        DECRYPT,
        DERIVE,
        SIGN,
        VERIFY
    }

    /**
     * @author Cedric Hammes
     * @since  29/12/2025
     */
    enum class Type {
        PRIVATE,
        PUBLIC,
        SECRET
    }
}
