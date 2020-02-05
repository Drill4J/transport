@file:Suppress("RemoveRedundantCallsOfConversionMethods", "RedundantSuspendModifier")

package com.epam.drill.transport.net

import com.epam.drill.internal.socket.setup_buffer_size
import com.epam.drill.internal.socket.socket_get_error
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import io.ktor.utils.io.internal.utils.KX_SOCKET
import platform.posix.EAGAIN
import platform.posix.init_sockets
import platform.posix.recv
import platform.posix.send
import kotlin.test.fail

abstract class NativeSocket constructor(@Suppress("RedundantSuspendModifier") val sockfd: KX_SOCKET) {
    companion object {
        init {
            init_sockets()
        }
    }

    abstract fun isAlive(): Boolean
    abstract fun setIsAlive(isAlive: Boolean): Unit

    private val availableBytes
        get() = run {
            if (!isAlive()) {
                error("closed")
            }
            getAvailableBytes(sockfd.toULong())
        }

    private fun recv(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
        var attempts = 50
        var result = 0
        while (true) {
            if (attempts-- <= 0)
                fail("Too many attempts to send")
            result += recv(sockfd, data.refTo(offset), count.convert(), 0).toInt()

            if (result < 0) {
                val error = socket_get_error()
                if (error == EAGAIN) continue
                fail("recv(): $error")
            }
            break
        }
        return result
    }

    fun tryRecv(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
        if (availableBytes <= 0) return -1
        return recv(data, offset, count)
    }

    private val mt = Mutex()

    suspend fun send(data: ByteArray, offset: Int = 0, count: Int = data.size - offset) {
        if (count <= 0) return
        var attempts = 100
        var remaining = count
        var coffset = offset
        memScoped {
            mt.withLock(this) {
                while (remaining > 0) {
                    if (attempts-- <= 0)
                        fail("Too many attempts to send")
                    val result = send(sockfd, data.refTo(coffset), remaining.convert(), 0).toInt()

                    if (result > 0) {
                        coffset += result
                        remaining -= result
                    }
                    if (result < count) {
                        if (isAllowedSocketError()) {
                            delay(100)
                            continue
                        }
                        fail("send(): ${socket_get_error()}")
                    }
                }
            }
        }
    }

    @Suppress("RemoveRedundantQualifierName")
    fun close() {
        close(sockfd.toULong())
        setIsAlive(false)
    }

    fun setNonBlocking() {
        setSocketNonBlocking(sockfd.toULong())
        setup_buffer_size(sockfd)
    }

    fun disconnect() {
        setIsAlive(false)
    }
}

suspend fun NativeSocket.suspendRecvUpTo(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
    if (count <= 0) return count

    while (true) {
        val read = tryRecv(data, offset, count)
        if (read <= 0) {
            delay(10L)
            continue
        }
        return read
    }
}


suspend fun NativeSocket.suspendSend(data: ByteArray, offset: Int = 0, count: Int = data.size - offset) {
    send(data, offset, count)
}
