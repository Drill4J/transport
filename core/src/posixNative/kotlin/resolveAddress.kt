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
//TODO remove if it is not used
actual fun resolveAddress(host: String, port: Int): Any = memScoped {

    val ip = IP.fromHost(host)
    val addr = allocArray<sockaddr_in>(1)
    addr.set(ip, port)
    @Suppress("UNCHECKED_CAST")
    addr as CValuesRef<sockaddr>

}

/**
 * return Socket
 */
actual fun getAddressInfoAndConnect(host: String, port: Int): Any = memScoped {
    val hints: CValue<addrinfo> = cValue {
        ai_family = AF_UNSPEC
        ai_socktype = SOCK_STREAM
        ai_flags = AI_PASSIVE
        ai_protocol = 0
    }

    val resultAddrInfo = alloc<CPointerVar<addrinfo>>()
    val code = getaddrinfo(host, port.toString(), hints, resultAddrInfo.ptr)
    defer { freeaddrinfo(resultAddrInfo.value) }

    val socketsAddress = getSockets(code, resultAddrInfo, host)

    return connectByFirstAcceptableSocket(socketsAddress)
}

//TODO move to common part
private fun connectByFirstAcceptableSocket(sockets: List<SocketAddress>): Int {
    var socket = 0
    for (socketAddress in sockets) {
        try {
            socketAddress.nativeAddress { address, size ->
                socket = socket(socketAddress.family.convert(), SOCK_STREAM, 0).convert()
                if (socket < 0) {
                    checkErrors("socket")
                }
                val connect = connect(socket.convert(), address, size)
                if (connect != 0) {
                    checkErrors("connect")
                }
            }
        } catch (x: Throwable) {
        }

    }
    return socket
}

private fun getSockets(
    code: Int,
    result: CPointerVar<addrinfo>,
    host: String
): List<SocketAddress> {
    when (code) {
        0 -> return result.pointed.toIpList()
        EAI_NONAME -> error("Bad host: $host")
//      EAI_ADDRFAMILY -> error("Bad address family")
        EAI_AGAIN -> error("Try again")
        EAI_BADFLAGS -> error("Bad flags")
//      EAI_BADHINTS -> error("Bad hint")
        EAI_FAIL -> error("FAIL")
        EAI_FAMILY -> error("Bad family")
//      EAI_MAX -> error("max reached")
        EAI_MEMORY -> error("OOM")
//      EAI_NODATA -> error("NO DATA")
        EAI_OVERFLOW -> error("OVERFLOW")
//      EAI_PROTOCOL -> error("PROTOCOL ERROR")
        EAI_SERVICE -> error("SERVICE ERROR")
        EAI_SOCKTYPE -> error("SOCKET TYPE ERROR")
        EAI_SYSTEM -> error("SYSTEM ERROR")
        else -> error("Unknown error: $code")
    }
}


sealed class SocketAddress(
    val family: sa_family_t,
    val port: Int
) {
    internal abstract fun nativeAddress(block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit)

    abstract val address: String
}

fun addrinfo?.toIpList(): List<SocketAddress> {
    var current: addrinfo? = this
    val result = mutableListOf<SocketAddress>()

    while (current != null) {
        result += current.ai_addr!!.pointed.toSocketAddress()
        current = current.ai_next?.pointed
    }

    return result
}

internal fun sockaddr.toSocketAddress(): SocketAddress = when (sa_family.toInt()) {
    AF_INET -> {
        val address = ptr.reinterpret<sockaddr_in>().pointed
        IPv4Address(address.sin_family, address.sin_addr, address.sin_port.convert())
    }
    AF_INET6 -> {
        val address = ptr.reinterpret<sockaddr_in6>().pointed
        IPv6Address(
            address.sin6_family,
            address.sin6_addr,
            address.sin6_port.convert(),
            address.sin6_flowinfo,
            address.sin6_scope_id
        )
    }
    else -> error("Unknown address family $sa_family")
}

internal class IPv4Address(
    family: sa_family_t,
    nativeAddress: in_addr,
    port: Int
) : SocketAddress(family, port) {
    private val ip: in_addr_t = nativeAddress.s_addr

    override fun nativeAddress(block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit) {
        cValue<sockaddr_in> {
            sin_addr.s_addr = ip
            sin_port = port.convert()
            sin_family = family

            block(ptr.reinterpret(), sockaddr_in.size.convert())
        }
    }

    override val address: String
        get() = error("String address representation is unsupported on Native.")

}

internal class IPv6Address(
    family: sa_family_t,
    rawAddress: in6_addr,
    port: Int,
    private val flowInfo: uint32_t,
    private val scopeId: uint32_t
) : SocketAddress(family, port) {
    private val ip = ByteArray(16) {
        0.toByte()
//        rawAddress.__u6_addr.__u6_addr8[it].toByte()
    }

    override fun nativeAddress(block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit) {
        cValue<sockaddr_in6> {
            sin6_family = family

            ip.forEachIndexed { index, byte ->
//                sin6_addr.__u6_addr.__u6_addr8[index] = byte.convert()
            }

            sin6_flowinfo = flowInfo
            sin6_port = port.convert()
            sin6_scope_id = scopeId

            block(ptr.reinterpret(), sockaddr_in6.size.convert())
        }
    }

    override val address: String
        get() = error("String address representation is unsupported on Native.")
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
