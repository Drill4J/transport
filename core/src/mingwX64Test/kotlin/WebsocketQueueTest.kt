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

//    TODO union macos & mingw tests

//    TODO when invoke "getaddrinfo" get error id = 10093, description = "Successful WSAStartup not yet performed.
//    TODO check on macosX64Test, this test is passed?
    @Test
    fun shouldCreateSocketAndConnect()= runBlocking {
        Logging.logLevel = LogLevel.TRACE
        AsyncClient("localhost", 8090, secure = false)
        checkErrors("getaddrinfo")
    }

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
