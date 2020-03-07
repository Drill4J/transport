package com.epam.drill.transport.ws

import com.epam.drill.transport.common.ws.URL
import com.epam.drill.transport.lang.toByteArray
import com.epam.drill.transport.net.AsyncClient
import com.epam.drill.transport.stream.readLine
import com.epam.drill.transport.stream.writeBytes
import com.epam.drill.util.encoding.toBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


suspend fun RWebsocketClient(
    url: String,
    protocols: List<String>? = emptyList(),
    origin: String? = "",
    wskey: String? = "",
    params: Map<String, String> = emptyMap()
): WebSocketClient {
    val uri = URL(url)
    val secure = when (uri.scheme) {
        "ws" -> false
        "wss" -> true
        else -> error("Unknown ws protocol ${uri.scheme}")
    }
    val host = uri.host ?: "127.0.0.1"
    val port = uri.defaultPort.takeIf { it != URL.DEFAULT_PORT } ?: if (secure) 443 else 80

    val client = AsyncClient(host, port, secure = secure)
    return RawSocketWebSocketClient(
        coroutineContext,
        client,
        uri,
        protocols,
        origin,
        wskey ?: "mykey",
        params
    ).apply {
        connect()
    }
}

class RawSocketWebSocketClient(
    override val coroutineContext: CoroutineContext,
    val client: AsyncClient,
    url: URL,
    protocols: List<String>?,
    val origin: String?,
    val key: String,
    val param: Map<String, String> = mutableMapOf()
) : WebSocketClient(url.fullUrl, protocols), CoroutineScope {
    val host = url.host ?: "127.0.0.1"
    val port = url.port
    val path = url.path

    internal suspend fun connect() {
        val data = (buildList<String> {
            add(
                "GET ${if (path.isEmpty()) {
                    url
                } else {
                    path
                }
                } HTTP/1.1"
            )
            add("Host: $host:$port")
            add("Pragma: no-cache")
            add("Cache-Control: no-cache")
            add("Upgrade: websocket")
            if (protocols != null) {
                add("Sec-WebSocket-Protocol: ${protocols.joinToString(", ")}")
            }
            add("Sec-WebSocket-Version: 13")
            add("Connection: Upgrade")
            add("Sec-WebSocket-Key: ${key.toByteArray().toBase64()}")
            add("Origin: $origin")
            add("User-Agent: Mozilla/5.0")
            param.forEach { (k, v) ->
                add("$k: $v")
            }
        }.joinToString("\r\n") + "\r\n\n").toByteArray()
        client.writeBytes(data)
        // Read response
        val headers = arrayListOf<String>()
        while (true) {
            val line = client.readLine().trimEnd()
            if (line.isEmpty()) {
                headers += line
                break
            }
        }

        launch {
            onOpen(Unit)
            try {
                launch {
                    try {
                        while (!closed) {
                            client.sendWsFrame(
                                WsFrame(
                                    "".toByteArray(),
                                    WsOpcode.Ping
                                )
                            )
                            delay(3000)
                        }
                    } catch (ignored: Throwable) {
                        client.disconnect()
                    }

                }

                loop@ while (!closed) {
                    val frame = client.readWsFrame()
                    @Suppress("IMPLICIT_CAST_TO_ANY") val payload =
                        if (frame.frameIsBinary) frame.data else frame.data.decodeToString()


                    when (frame.type) {
                        WsOpcode.Close -> {
                            break@loop
                        }
                        WsOpcode.Ping -> {
                            client.sendWsFrame(
                                WsFrame(
                                    frame.data,
                                    WsOpcode.Pong
                                )
                            )
                        }
                        WsOpcode.Pong -> {
                            //todo
                            lastPong = 100
                        }
                        else -> {
                            when (payload) {
                                is String -> onStringMessage.forEach { it(payload) }
                                is ByteArray -> onBinaryMessage.forEach { it(payload) }
                            }
                            onAnyMessage.forEach { it(payload) }
                        }
                    }
                }
            } catch (e: Throwable) {
                onError(e)
            }
            onClose(Unit)

        }

    }

    private var lastPong: Long = 0

    var closed = false

    override fun close(code: Int, reason: String) {
        closed = true
        launch {
            client.sendWsFrame(
                WsFrame(
                    byteArrayOf(),
                    WsOpcode.Close
                )
            )
        }
    }

    override suspend fun send(message: String) {
        client.sendWsFrame(
            WsFrame(
                message.encodeToByteArray(),
                WsOpcode.Text
            )
        )
    }

    override suspend fun send(message: ByteArray) {
        client.sendWsFrame(
            WsFrame(
                message,
                WsOpcode.Binary
            )
        )
    }
}
