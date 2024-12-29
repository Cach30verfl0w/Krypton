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

package de.cacheoverflow.krypton.core.element

/**
 * Represents an element that can be queried using a specific key. Each QueryableElement has a unique key and provides functionality to
 * retrieve itself if the given key matches its key. Additionally, multiple QueryableElement instances can be combined into a single
 * composite element.
 *
 * @author Cedric Hammes
 * @since  27/10/2024
 */
interface QueryableElement {
    val key: Key<*>
    @Suppress("UNCHECKED_CAST")
    operator fun <T : QueryableElement> get(key: Key<T>): T? = if (key == this.key) this as T else null
    operator fun plus(other: QueryableElement): QueryableElement = Combined(this, other)
    interface Key<T>

    /**
     * A private implementation of a composite QueryableElement. Combines two QueryableElement instances and provides a unified interface
     * to query them.
     *
     * @author Cedric Hammes
     * @since  27/10/2024
     */
    private class Combined(private val first: QueryableElement, private val second: QueryableElement) : QueryableElement {
        override val key: Key<*> = Combined
        override fun <T : QueryableElement> get(key: Key<T>): T? = first[key]?: second[key]
        private companion object : Key<Combined>
    }
}
