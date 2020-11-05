package com.epam.drill.transport.net

import com.epam.drill.transport.stream.AsyncCloseable
import com.epam.drill.transport.stream.AsyncInputStream
import com.epam.drill.transport.stream.AsyncOutputStream

abstract class AsyncSocketFactory {
    abstract suspend fun createClient(
        networkAddress: NetworkAddress,
        secure: Boolean = false
    ): NativeAsyncSocketFactory.NativeAsyncClient
}

@SharedImmutable
internal val asyncSocketFactory: AsyncSocketFactory =
    NativeAsyncSocketFactory

interface AsyncClient : AsyncStream {
    suspend fun connect()
    fun disconnect()

    companion object {
        suspend operator fun invoke(host: String, port: Int, secure: Boolean = false) =
            createAndConnect(host, port, secure)

        private suspend fun createAndConnect(
            host: String,
            port: Int,
            secure: Boolean = false
        ): NativeAsyncSocketFactory.NativeAsyncClient {
            val socket = asyncSocketFactory.createClient(
                NetworkAddress(hostname = host, port = port),
                secure
            )

            socket.connect()
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
