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

import com.epam.drill.transport.stream.AsyncCloseable
import com.epam.drill.transport.stream.AsyncInputStream
import com.epam.drill.transport.stream.AsyncOutputStream

abstract class AsyncSocketFactory {
    abstract suspend fun createClient(secure: Boolean = false): NativeAsyncSocketFactory.NativeAsyncClient
}

@SharedImmutable
internal val asyncSocketFactory: AsyncSocketFactory =
    NativeAsyncSocketFactory

interface AsyncClient : AsyncStream {
    suspend fun connect(host: String, port: Int)
    fun disconnect()

    companion object {
        suspend operator fun invoke(host: String, port: Int, secure: Boolean = false) =
            createAndConnect(host, port, secure)

        private suspend fun createAndConnect(host: String, port: Int, secure: Boolean = false): NativeAsyncSocketFactory.NativeAsyncClient {
            val socket = asyncSocketFactory.createClient(secure)

            socket.connect(host, port)
            return socket
        }
    }
}

interface AsyncStream : AsyncInputStream,
    AsyncOutputStream,
    AsyncCloseable {

    val connected: Boolean
    override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int
    override suspend fun write(buffer: ByteArray, offset: Int, len: Int)
    override suspend fun close()

}