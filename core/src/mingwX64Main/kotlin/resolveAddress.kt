package com.epam.drill.transport.net

import com.epam.drill.internal.socket.socket_get_error
import com.epam.drill.logger.Logging
import kotlinx.cinterop.*
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.FIONBIO
import platform.posix.FIONREAD
import platform.posix.IPPROTO_TCP
import platform.posix.SOCK_STREAM
import platform.posix.AF_UNSPEC
import platform.posix.closesocket
import platform.posix.connect
import platform.posix.ioctlsocket
import platform.posix.socket
import platform.windows.*

@SharedImmutable
private val logger = Logging.logger("SocketClient")

//TODO remove if it is not used
actual fun resolveAddress(host: String, port: Int): Any = memScoped {
    logger.trace { "try to resolve address for host:'${host}' port:'${port}'" }
    val addr = allocArray<LPADDRINFOVar>(1)
    val alloc = alloc<platform.windows.addrinfo>()
    alloc.ai_family = AF_INET
    alloc.ai_socktype = SOCK_STREAM
    alloc.ai_protocol = IPPROTO_TCP
    platform.windows.getaddrinfo(host, port.toString(), alloc.ptr, addr)
    logger.trace { "address resolved $addr first ${addr[0]}" }
    val info = addr[0]!!.pointed
    val aiAddr: CPointer<sockaddr> = info.ai_addr!!
    logger.trace { "info resolved $aiAddr" }
    aiAddr as CValuesRef<sockaddr>
}

/**
 * return socket
 * todo calculate socket by host&port
 */
actual fun getAddressInfoAndConnect(host: String, port: Int): Any = memScoped {
    logger.debug { "getAddressInfoAndConnect to ${host}:${port}" }
    val hints: CValue<addrinfo> = cValue {
        ai_family = AF_UNSPEC
        ai_socktype = SOCK_STREAM
        ai_flags = AI_PASSIVE
        ai_protocol = 0
    }

    val resultAddrInfo = alloc<CPointerVar<addrinfo>>()
    val code = getaddrinfo(host, port.toString(), hints, resultAddrInfo.ptr)
    defer { freeaddrinfo(resultAddrInfo.value) }
    logger.debug { "resultAddrInfo $resultAddrInfo" }
    val socketsAddress = getSockets(code, resultAddrInfo, host)

    logger.debug { "socketsAddress $socketsAddress" }
    return connectByFirstAcceptableSocket(socketsAddress)

}
//TODO move to common part
private fun connectByFirstAcceptableSocket(sockets: List<SocketAddress>): ULong {
    var socketLong : ULong = 0.toULong()
    for (socketAddress in sockets) {
        try {
            socketAddress.nativeAddress { address, size ->
                socketLong = socket(socketAddress.family.convert(), SOCK_STREAM, 0)
                val socket : Int = socketLong.convert()
                logger.debug { "try to connect by socket $socket" }
                if (socket < 0) {
                    checkErrors("socket")
                }
                logger.debug { "try to connect by socket $socket $address" }
                val connect = connect(socket.convert(), address, size)
                logger.debug { "connect $connect" }
                if (connect != 0) {
                    checkErrors("connect")
                }
            }
        } catch (x: Throwable) {
        }

    }
    return socketLong
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
//        EAI_OVERFLOW -> error("OVERFLOW")
//      EAI_PROTOCOL -> error("PROTOCOL ERROR")
        EAI_SERVICE -> error("SERVICE ERROR")
        EAI_SOCKTYPE -> error("SOCKET TYPE ERROR")
//        EAI_SYSTEM -> error("SYSTEM ERROR")
        else -> error("when it is getting sockets unknown error : $code")
    }
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

sealed class SocketAddress(
    val family: Short,
    val port: Int
) {
    internal abstract fun nativeAddress(block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit)

    abstract val address: String
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
    family: Short,
    nativeAddress: in_addr,
    port: Int
) : SocketAddress(family, port) {
    private val ip: UInt = nativeAddress.S_un.S_addr

    override fun nativeAddress(block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit) {
        cValue<sockaddr_in> {
//            TODO check
            sin_addr.S_un.S_addr = ip
            sin_port = port.convert()
            sin_family = family

            block(ptr.reinterpret(), sockaddr_in.size.convert())
        }
    }

    override val address: String
        get() = error("String address representation is unsupported on Native.")

}

internal class IPv6Address(
    family: Short,
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

actual fun getAvailableBytes(sockRaw: ULong): Int {
    val bytes_available = intArrayOf(0, 0)
    @Suppress("UNCHECKED_CAST")
    ioctlsocket(
        sockRaw,
        FIONREAD,
        bytes_available.refTo(0) as CValuesRef<u_longVar>
    )
    return bytes_available[0]

}

actual fun close(sockRaw: ULong) {
    closesocket(sockRaw)
}

actual fun setSocketBlocking(sockRaw: ULong, is_blocking: Boolean) = memScoped<Unit> {
    val mode = alloc<u_longVar>()
    if (is_blocking)
        mode.value = 0.convert()
    else
        mode.value = 1.convert()
    (ioctlsocket(sockRaw, FIONBIO.convert(), mode.ptr) == 0)
}

actual fun getError(): Int = socket_get_error()
