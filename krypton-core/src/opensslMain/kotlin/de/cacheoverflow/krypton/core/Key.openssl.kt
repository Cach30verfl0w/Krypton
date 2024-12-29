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

import de.cacheoverflow.krypton.openssl.*
import kotlinx.cinterop.CPointer

/**
 * @author Cedric Hammes
 * @since  29/12/2025
 */
actual class Key internal constructor(
    actual val algorithm: String,
    actual val usages: Array<Usage>,
    actual val type: Type,
    private val body: KeyBody
) : AutoCloseable {

    actual override fun close() = body.close()

    /**
     * @author Cedric Hammes
     * @since  29/12/2025
     */
    actual enum class Usage {
        ENCRYPT, DECRYPT, DERIVE, SIGN, VERIFY
    }

    /**
     * @author Cedric Hammes
     * @since  29/12/2025
     */
    actual enum class Type {
        PRIVATE,
        PUBLIC,
        SECRET
    }

    /**
     * @author Cedric Hammes
     * @since  29/12/2025
     */
    sealed interface KeyBody : AutoCloseable {
        fun size(): Int

        /**
         * @author Cedric Hammes
         * @since  29/12/2025
         */
        class DataKeyBody internal constructor(private val data: CPointer<BIO>) : KeyBody {
            override fun size(): Int = BIO_ctrl_pending(data).toInt()

            override fun close() {
                BIO_free(data)
            }
        }

        /**
         * @author Cedric Hammes
         * @since  29/12/2025
         */
        class EVPKeyBody internal constructor(private val key: CPointer<EVP_PKEY>) : KeyBody {
            override fun size(): Int = EVP_PKEY_get_bits(key)

            override fun close() {
                EVP_PKEY_free(key)
            }
        }

    }
}
