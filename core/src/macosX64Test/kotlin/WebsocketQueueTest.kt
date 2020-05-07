package ws

import Echo.startServer
import TestBase
import com.epam.drill.core.concurrency.*
import com.epam.drill.logger.*
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
    @Ignore
    @Test
    fun shouldProcessBigMessage() = runTest(1.minutes) {
        val (serverFD, port) = startServer()
        val veryBigMessage = StringBuilder().apply { repeat(MESSAGE_SIZE) { append(".") } }.toString()
        val wsClient = RWebsocketClient("ws://localhost:$port")
        wsClient.onStringMessage.add { stringMessage ->
            assertEquals(veryBigMessage.length, stringMessage.length)
            (wsClient as? RawSocketWebSocketClient)?.client?.sendWsFrame(WsFrame(byteArrayOf(), WsOpcode.Close))
        }
        wsClient.onClose.add {
            wsClient.close()
            close(serverFD.toULong())
        }
        wsClient.send(veryBigMessage.apply {
            val mb: Float = this.encodeToByteArray().size / 1024f / 1024f
            println("$mb megabyte")
        })

    }

    @Ignore
    @Test
    fun shouldProcessMultipleMessages() = runTest(5.minutes) {
        val messageForSend = "any"
        logConfig
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
