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
@file:Suppress("RemoveRedundantQualifierName")

package com.epam.drill.transport.net

import com.epam.drill.internal.socket.*
import kotlinx.cinterop.*
import platform.posix.*

class IP(val data: UByteArray) {

    val v0 get() = data[0]
    val v1 get() = data[1]
    val v2 get() = data[2]
    val v3 get() = data[3]
    val str get() = "$v0.$v1.$v2.$v3"
    val value: Int get() = (v0.toInt() shl 0) or (v1.toInt() shl 8) or (v2.toInt() shl 16) or (v3.toInt() shl 24)

    //val value: Int get() = (v0.toInt() shl 24) or (v1.toInt() shl 16) or (v2.toInt() shl 8) or (v3.toInt() shl 0)
    override fun toString(): String = str

    companion object {
        fun fromHost(host: String): IP {
            val hname = gethostbyname(host)
            val inetaddr = hname!!.pointed.h_addr_list!![0]!!
            return IP(
                ubyteArrayOf(
                    inetaddr[0].toUByte(),
                    inetaddr[1].toUByte(),
                    inetaddr[2].toUByte(),
                    inetaddr[3].toUByte()
                )
            )
        }
    }
}

fun CPointer<sockaddr_in>.set(ip: IP, port: Int) {
    val addr = this
    addr.pointed.sin_family = AF_INET.convert()
    addr.pointed.sin_addr.s_addr = ip.value.toUInt()
    addr.pointed.sin_port = swapBytes(port.toUShort())
}

actual fun resolveAddress(host: String, port: Int): Any = memScoped {

    val ip = IP.fromHost(host)
    val addr = allocArray<sockaddr_in>(1)
    addr.set(ip, port)
    @Suppress("UNCHECKED_CAST")
    addr as CValuesRef<sockaddr>

}

fun swapBytes(v: UShort): UShort =
    (((v.toInt() and 0xFF) shl 8) or ((v.toInt() ushr 8) and 0xFF)).toUShort()


actual fun getAvailableBytes(sockRaw: ULong): Int {
    val bytes_available = intArrayOf(0, 0)
    ioctl(sockRaw.toInt(), FIONREAD, bytes_available.refTo(0))
    return bytes_available[0]

}

actual fun close(sockRaw: ULong) {
    platform.posix.shutdown(sockRaw.toInt(), SHUT_RDWR)
}

actual fun setSocketBlocking(sockRaw: ULong, is_blocking: Boolean) {
    val flags = fcntl(sockRaw.toInt(), F_GETFL, 0)
    if (flags == -1) return
    if (is_blocking)
        fcntl(sockRaw.convert(), F_SETFL, flags xor O_NONBLOCK)
    else
        fcntl(sockRaw.convert(), F_SETFL, flags or O_NONBLOCK)
}

actual fun getError() = socket_get_error()
