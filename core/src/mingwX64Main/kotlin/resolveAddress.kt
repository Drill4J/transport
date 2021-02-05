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

import com.epam.drill.internal.socket.socket_get_error
import com.epam.drill.logger.*
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.LPADDRINFOVar

@SharedImmutable
private val logger = Logging.logger("SocketClient")

actual fun resolveAddress(host: String, port: Int): Any = memScoped {
    logger.trace { "try to resolve address for host:'${host}' port:'${port}'" }
    val addr = allocArray<LPADDRINFOVar>(1)
    val alloc = alloc<platform.windows.addrinfo>()
    alloc.ai_family = AF_INET
    alloc.ai_socktype = SOCK_STREAM
    alloc.ai_protocol = IPPROTO_TCP
    platform.windows.getaddrinfo(host, port.toString(), alloc.ptr, addr)
    logger.trace { "address resolved $addr" }
    val info = addr[0]!!.pointed
    val aiAddr: CPointer<sockaddr> = info.ai_addr!!
    logger.trace { "info resolved $aiAddr" }
    aiAddr as CValuesRef<sockaddr>
}

actual fun getAvailableBytes(sockRaw: ULong): Int {
    val bytes_available = intArrayOf(0, 0)
    @Suppress("UNCHECKED_CAST")
    ioctlsocket(
        sockRaw,
        FIONREAD,
        bytes_available.refTo(0) as CValuesRef<u_longVar>
    )
    return bytes_available[0]

}

actual fun close(sockRaw: ULong) {
    closesocket(sockRaw)
}

actual fun setSocketBlocking(sockRaw: ULong, is_blocking: Boolean) = memScoped<Unit> {
    val mode = alloc<u_longVar>()
    if (is_blocking)
        mode.value = 0.convert()
    else
        mode.value = 1.convert()
    (ioctlsocket(sockRaw, FIONBIO.convert(), mode.ptr) == 0)
}

actual fun getError(): Int = socket_get_error()
