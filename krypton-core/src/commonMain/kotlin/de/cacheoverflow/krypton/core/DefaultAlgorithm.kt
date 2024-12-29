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

import de.cacheoverflow.krypton.core.element.QueryableElement
import de.cacheoverflow.krypton.core.annotation.UnstableAlgorithmAPI
import de.cacheoverflow.krypton.core.element.AbstractReadKeyElement

internal expect fun defaultReadKeyElement(algorithm: Algorithm): AbstractReadKeyElement

/**
 * This enum provides the by-default available algorithms surely supported by the Krypton library itself. This enum contains both deprecated
 * and modern algorithms. Most of the algorithms are standardized and stabilized, the unstable ones are marked with [UnstableAlgorithmAPI].
 *
 * Below this text you can see a list of all algorithms supported by default (a few of them on not all platforms, mostly JS is not
 * supported by conditionally-supported algorithms):
 * - **Digests:** SHA-1, SHA-2 family, SHA-3 family and MD5
 * - **Key Agreement algorithm:** DH and ECDH
 * - **Signature algorithms:** RSA
 * - **Cipher algorithm:** RSA and RSA
 *
 * @author Cedric Hammes
 * @since  28/09/2024
 */
enum class DefaultAlgorithm(override val literal: String, override var provider: Lazy<QueryableElement>) : Algorithm {
    @UnstableAlgorithmAPI
    AES("AES", lazy { defaultReadKeyElement(AES) });

    override fun toString(): String = literal
}
