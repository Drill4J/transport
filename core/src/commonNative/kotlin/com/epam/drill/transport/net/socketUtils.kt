package com.epam.drill.transport.net

import kotlinx.cinterop.*
import platform.posix.*

fun NetworkAddress.resolve(): List<SocketAddress> = getAddressInfo(hostname, port)
internal fun getAddressInfo(
        hostname: String,
        portInfo: Int
): List<SocketAddress> = memScoped {
    val hints: CValue<addrinfo> = cValue {
        ai_family = AF_UNSPEC
        ai_socktype = SOCK_STREAM
        ai_flags = AI_PASSIVE
        ai_protocol = 0
    }

    val result = alloc<CPointerVar<addrinfo>>()
    val code = getaddrinfo(hostname, portInfo.toString(), hints, result.ptr)
    defer { freeaddrinfo(result.value) }

    when (code) {
        0 -> return result.pointed.toIpList()
        EAI_NONAME -> error("Bad hostname: $hostname")
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

internal fun addrinfo?.toIpList(): List<SocketAddress> {
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

class NetworkAddress(
        val hostname: String,
        val port: Int
) {

    /**
     * Resolve current socket address.
     */

    override fun toString(): String = "NetworkAddress[$hostname:$port]"
}

val NetworkAddress.port: Int get() = port

sealed class SocketAddress(
        val family: sa_family_t,
        val port: Int
) {
    internal abstract fun nativeAddress(block: (address: CPointer<sockaddr>, size: socklen_t) -> Unit)

    abstract val address: String
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
