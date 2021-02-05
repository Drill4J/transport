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
package com.epam.drill.transport.ws

abstract class WebSocketClient protected constructor(
    val url: String,
    val protocols: List<String>?
) {
    val onOpen = mutableSetOf<() -> Unit>()
    val onError = mutableSetOf<(Throwable) -> Unit>()
    val onClose = mutableSetOf<() -> Unit>()

    val onBinaryMessage = mutableSetOf<suspend (ByteArray) -> Unit>()
    val onStringMessage = mutableSetOf<suspend (String) -> Unit>()
    val onAnyMessage = mutableSetOf<suspend (Any) -> Unit>()

    open fun close(code: Int = 0, reason: String = ""): Unit = Unit
    open suspend fun send(message: String): Unit = Unit
    open suspend fun send(message: ByteArray): Unit = Unit
    open fun blockingSend(message: ByteArray): Unit = Unit
}