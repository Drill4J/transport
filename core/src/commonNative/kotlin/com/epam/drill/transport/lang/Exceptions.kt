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
package com.epam.drill.transport.lang

open class IOException constructor(msg: String) : Exception(msg)
open class EOFException constructor(msg: String) : IOException(msg)
class InvalidOperationException(str: String = "Invalid Operation") : Exception(str)

fun invalidOp(msg: String): Nothing = throw InvalidOperationException(
    msg
)
fun unsupported(): Nothing = throw UnsupportedOperationException("unsupported")

