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
package ws

import Echo.startServer
import TestBase
import com.epam.drill.core.concurrency.*
import com.epam.drill.logger.*
import com.epam.drill.logger.api.*
import com.epam.drill.transport.common.ws.*
import com.epam.drill.transport.exception.*
import com.epam.drill.transport.net.*
import com.epam.drill.transport.ws.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.native.concurrent.*
import kotlin.test.*
import kotlin.time.*


val ws = AtomicReference(Channel<ByteArray>().freeze()).freeze()

class WebsocketQueueTest : TestBase() {

    @Test
    fun shouldProcessBigMessage() = runTest(2.minutes) {
        val (serverFD, port) = startServer()
        Logging.logLevel = LogLevel.TRACE
        val wsClient = RWebsocketClient("ws://localhost:$port")
        delay(2000)
        wsClient.onBinaryMessage.add { stringMessage ->
            println(stringMessage.size)
            (wsClient as? RawSocketWebSocketClient)?.client?.sendWsFrame(WsFrame(byteArrayOf(), WsOpcode.Close))
        }
        wsClient.onClose.add {
            wsClient.close()
            close(serverFD.toULong())
        }
        Worker.start(true).execute(TransferMode.UNSAFE, { wsClient }) {
            it.blockingSend(ByteArray(Int.MAX_VALUE - 100))
        }
    }

    @Ignore
    @Test
    fun shouldProcessMultipleMessages() = runTest(5.minutes) {
        val messageForSend = "any"
        val (_, port) = startServer()
        val currentMessageIndex = atomic(1)
        val wsClient = RWebsocketClient(
            url = URL(
                scheme = "ws",
                userInfo = null,
                host = "localhost",
                path = "",
                query = "",
                fragment = null,
                port = port
            ).fullUrl,
            protocols = listOf("x-kaazing-handshake"),
            origin = "",
            wskey = "",
            params = mutableMapOf()
        )
        wsClient.onOpen += {
            println("Opened")
        }

        wsClient.onBinaryMessage.add { binary ->
            println(binary)
        }

        wsClient.onStringMessage.add { stringMessage ->
            assertEquals(messageForSend.length, stringMessage.length)
            if (currentMessageIndex.incrementAndGet() == ITERATIONS) {
                (wsClient as? RawSocketWebSocketClient)?.client?.sendWsFrame(
                    WsFrame(
                        byteArrayOf(),
                        WsOpcode.Close
                    )
                )
            }
        }
        wsClient.onError.add {
            println("AHAH")
            if (it is WsException) {
                println(it.message)
            } else {
                it.printStackTrace()
            }
        }
        wsClient.onClose.add {
            (wsClient as? RawSocketWebSocketClient)?.closed = true
        }

        launch {
            try {
                supervisorScope {

                    while (true) {
                        delay(10)
                        wsClient.send(ws.value.receive())
                    }
                }
            } catch (ex: Throwable) {
                wsClient.onError.forEach { it(ex) }
            }
        }

        delay(3000)
        Worker.start(true).execute(TransferMode.UNSAFE, {}) {
            repeat(ITERATIONS) {
                BackgroundThread {
                    ws.value.send(ByteArray(Int.MAX_VALUE - 1000))
                }
            }
        }
    }

    companion object {
        private const val ITERATIONS = 1000
        private const val MESSAGE_SIZE = 52309000
    }
}
