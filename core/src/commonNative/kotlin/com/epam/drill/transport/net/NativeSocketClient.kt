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
import io.ktor.utils.io.internal.utils.*
import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import platform.posix.*

class NativeSocketClient(sockfd: KX_SOCKET) : NativeSocket(sockfd) {
    companion object {
        operator fun invoke(): NativeSocketClient {
            val socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)
            return NativeSocketClient(socket)
        }
    }

    private var _connected = atomic(false)
    override fun isAlive() = _connected.value
    override fun setIsAlive(isAlive: Boolean) {
        _connected.value = isAlive
    }

    @Suppress("RemoveRedundantCallsOfConversionMethods")
    fun connect(host: String, port: Int) {
        memScoped {
            @Suppress("UNCHECKED_CAST")
            val inetaddr = resolveAddress(host, port) as CValuesRef<sockaddr>
            checkErrors("getaddrinfo")

            @Suppress("RemoveRedundantCallsOfConversionMethods") val connected =
                connect(sockfd, inetaddr, sockaddr_in.size.convert())
            checkErrors("connect to ${host}:${port}")
            setNonBlocking()
            if (connected != 0) {
                _connected.value = false
                throw ConnectException()
            } else {
                _connected.value = true
            }
        }
    }
}