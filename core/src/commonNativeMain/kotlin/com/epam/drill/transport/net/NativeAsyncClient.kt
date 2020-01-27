package com.epam.drill.transport.net

class NativeAsyncServer(val socket: NativeSocketServer) :
    AsyncStream {

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