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

import java.security.Key
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

/**
 * @author Cedric Hammes
 * @since  29/12/2025
 */
actual class Key internal constructor(internal: Key, actual val usages: Array<Usage>) : AutoCloseable {
    actual val algorithm: String = internal.algorithm
    actual val type: Type = when(internal) {
        is SecretKey -> Type.SECRET
        is PrivateKey -> Type.PRIVATE
        is PublicKey -> Type.PUBLIC
        else -> throw IllegalArgumentException("Key type ${internal::class.qualifiedName} is not supported by Krypton Key")
    }

    actual override fun close() {} // Nothing to close

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
}
