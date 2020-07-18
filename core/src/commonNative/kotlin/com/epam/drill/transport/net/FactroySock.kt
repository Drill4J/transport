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
