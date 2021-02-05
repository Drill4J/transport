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



object NativeAsyncSocketFactory : AsyncSocketFactory() {
    class NativeAsyncClient(val socket: NativeSocketClient) :
        AsyncClient {
        override fun disconnect() {
            socket.disconnect()
        }

        override suspend fun connect(host: String, port: Int) {
            socket.connect(host, port)
        }

        override val connected: Boolean get() = socket.isAlive()

        override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int {
            return socket.suspendRecvUpTo(buffer, offset, len)
        }

        override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
            socket.suspendSend(buffer, offset, len)
        }

        override suspend fun close() {
            socket.close()
        }
    }


    override suspend fun createClient(secure: Boolean): NativeAsyncClient {
        return NativeAsyncClient(
            NativeSocketClient()
        )
    }

}
