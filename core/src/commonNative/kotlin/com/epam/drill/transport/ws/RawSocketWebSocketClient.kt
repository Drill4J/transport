package com.epam.drill.transport.ws

import com.epam.drill.transport.common.ws.*
import com.epam.drill.transport.lang.*
import com.epam.drill.transport.net.*
import com.epam.drill.transport.stream.*
import com.epam.drill.util.encoding.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import mu.*
import kotlin.coroutines.*
import kotlin.native.concurrent.*

@SharedImmutable
private val logger = KotlinLogging.logger("RawSocketWebSocketClient")

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

    var closed: Boolean
        get() = _closed.value
        private set(value) = _closed.update { value }

    private val _closed = atomic(false)
    private val _ping = atomic(true)

    internal fun connect() {
        val request = (buildList<String> {
            add("GET ${path.ifEmpty { url }} HTTP/1.1")
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
        }.joinToString("\r\n") + "\r\n\n")
        launch {
            try {
                logger.debug { "connecting to $host:$port..." }
                withTimeout(10000L) {
                    logger.trace { "request:\n$request" }
                    client.writeBytes(request.toByteArray())
                    // Read response
                    val response = StringBuilder()
                    val headLine = client.readLine().trimEnd()
                    val (_, status) = headLine.split(" ")
                    if (status.toInt() != 101) {
                        throw RuntimeException("Can't connect to the server: $headLine")
                    }
                    while (true) {
                        val line = client.readLine().trimEnd()
                        if (line.isEmpty()) {
                            break
                        } else response.appendln(line)
                    }
                    logger.trace { "response:\n$response" }
                }
                logger.debug { "connected to $host:$port" }
                onOpen.forEach { it() }
                launch {
                    val pingFrame = WsFrame(data = ByteArray(0), type = WsOpcode.Ping)
                    _ping.value = true
                    try {
                        while (!closed) {
                            if (_ping.value) {
                                logger.trace { "ping>" }
                                client.sendWsFrame(pingFrame)
                                withTimeout(10000L ) {
                                    while (_ping.value) {
                                        delay(500L)
                                    }
                                }
                            } else {
                                _ping.value = true
                                delay(3000L)
                            }
                        }
                    } catch (e: Throwable) {
                        logger.error(e) { "Ping timeout!" }
                        logger.info { "closing client" }
                        client.close()
                        logger.info { "client cloased" }
                    }
                }

                loop@ while (!closed) {
                    val frame = client.readWsFrame()
                    val payload: Any = if (!frame.frameIsBinary) {
                        frame.data.decodeToString()
                    } else frame.data
                    _ping.value = false

                    when (frame.type) {
                        WsOpcode.Close -> {
                            logger.trace { "<close" }
                            break@loop
                        }
                        WsOpcode.Ping -> {
                            logger.trace { "<ping" }
                            client.sendWsFrame(
                                WsFrame(
                                    frame.data,
                                    WsOpcode.Pong
                                )
                            )
                        }
                        WsOpcode.Pong -> {
                            logger.trace { "<pong" }
                        }
                        else -> {
                            launch {
                                logger.trace { "<message" }
                                when (payload) {
                                    is String -> onStringMessage.forEach { it(payload) }
                                    is ByteArray -> onBinaryMessage.forEach { it(payload) }
                                }
                                onAnyMessage.forEach { it(payload) }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                onError.forEach { it(e) }

            }
            onClose.forEach { it() }
        }
    }

    override fun close(code: Int, reason: String) {
        logger.debug { "closing socket: code=$code, reason=$reason" }
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
