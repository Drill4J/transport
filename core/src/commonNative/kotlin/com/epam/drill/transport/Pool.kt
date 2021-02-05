/**
 * Copyright 2020 EPAM Systems
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
package com.epam.drill.transport

class Pool<T>(private val reset: (T) -> Unit = {}, preallocate: Int = 0, private val gen: (Int) -> T) {
    constructor(preallocate: Int = 0, gen: (Int) -> T) : this({}, preallocate, gen)

    private val items = Stack<T>()
    private var lastId = 0

    init {
        for (n in 0 until preallocate) items.push(gen(lastId++))
    }

    fun alloc(): T = if (items.isNotEmpty()) items.pop() else gen(lastId++)

    fun free(element: T) {
        reset(element)
        items.push(element)
    }

}

class Stack<TGen> : Collection<TGen> {
    private val items = arrayListOf<TGen>()

    override val size: Int get() = items.size
    override fun isEmpty() = size == 0

    fun push(v: TGen) = run { items.add(v) }
    fun pop(): TGen = items.removeAt(items.size - 1)

    override fun contains(element: TGen): Boolean = items.contains(element)
    override fun containsAll(elements: Collection<TGen>): Boolean = items.containsAll(elements)
    override fun iterator(): Iterator<TGen> = items.iterator()
}
