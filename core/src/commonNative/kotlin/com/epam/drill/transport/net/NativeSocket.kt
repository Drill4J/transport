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
@file:Suppress("RemoveRedundantCallsOfConversionMethods", "RedundantSuspendModifier")

package com.epam.drill.transport.net

import com.epam.drill.internal.socket.*
import com.epam.drill.transport.exception.*
import io.ktor.utils.io.internal.utils.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.*

abstract class NativeSocket constructor(@Suppress("RedundantSuspendModifier") val sockfd: KX_SOCKET) {
    companion object {
        init {
            init_sockets()
        }
    }

    abstract fun isAlive(): Boolean
    abstract fun setIsAlive(isAlive: Boolean)

    private val availableBytes
        get() = run {
            if (!isAlive()) {
                throw AlreadyClosedException("closed")
            }
            getAvailableBytes(sockfd.toULong())
        }

    private fun recv(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
        var attempts = 50
        var result = 0
        while (true) {
            if (attempts-- <= 0)
                throw ReadRetryException("Too many attempts to recv")
            result += recv(sockfd, data.refTo(offset), count.convert(), 0).toInt()

            if (result < 0) {
                val error = socket_get_error()
                if (error == EAGAIN) continue
                throwError("recv", error)
            }
            break
        }
        return result
    }

    fun tryRecv(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): Int {
        if (availableBytes <= 0) return -1
        return recv(data, offset, count)
    }


    suspend fun send(data: ByteArray, offset: Int = 0, count: Int = data.size - offset) {
        if (count <= 0) return
        var attempts = 100
        var remaining = count
        var coffset = offset
        memScoped {
            while (remaining > 0) {
                if (attempts-- <= 0)
                    throw SendRetryException("Too many attempts to send")
                val result = send(sockfd, data.refTo(coffset), remaining.convert(), 0).toInt()

                if (result > 0) {
                    coffset += result
                    remaining -= result
                }
                if (result < count) {
                    val socketError = getError()

                    if (socketError == EAGAIN_ERROR || socketError == 316 || socketError == 0) {
                        delay(100)
                        continue
                    }
                    throwError("send", socketError)
                }
            }
        }
    }

    fun blockingSend(data: ByteArray, offset: Int = 0, count: Int = data.size - offset) {
        if (count <= 0) return
        var remaining = count
        var coffset = offset
        setSocketBlocking(sockfd.toULong(), true)
        try {
            while (remaining > 0) {
                val result = send(sockfd, data.refTo(coffset), remaining.convert(), 0).toInt()
                if (result > 0) {
                    coffset += result
                    remaining -= result
                }
                if (result < count) {
                    val socketError = getError()
                    if (socketError == EAGAIN_ERROR || socketError == 316 || socketError == 0) {
                        sleep(1)
                        continue
                    }
                    throwError("send", socketError)
                }
            }
        } finally {
            setSocketBlocking(sockfd.toULong(), false)
        }
    }


    @Suppress("RemoveRedundantQualifierName")
    fun close() {
        close(sockfd.toULong())
        setIsAlive(false)
    }

    fun setNonBlocking() {
        setSocketBlocking(sockfd.toULong(), false)
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
