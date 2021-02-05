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
package com.epam.drill.transport.net

import com.epam.drill.transport.exception.*

expect fun resolveAddress(host: String, port: Int): Any

expect fun getAvailableBytes(sockRaw: ULong): Int

expect fun close(sockRaw: ULong)

expect fun setSocketBlocking(sockRaw: ULong, is_blocking: Boolean)

expect fun getError(): Int

expect val EAGAIN_ERROR: Int

fun isAllowedSocketError(): Boolean {
    val socketError = getError()
    return socketError == EAGAIN_ERROR || socketError == 316 || socketError == 0
}

fun checkErrors(name: String) {
    val error = getError()
    if (error != 0) {
        throwError(name, error)
    }
}

fun throwError(name: String, code: Int) {
    throw WsException("error($name): ${errorsMapping[code]?.description ?: "unknown error($code)"}")
}