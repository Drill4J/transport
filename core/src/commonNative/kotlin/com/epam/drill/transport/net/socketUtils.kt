package com.epam.drill.transport.net

import io.ktor.utils.io.internal.utils.KX_SOCKET

fun NetworkAddress.resolve(): KX_SOCKET = getAddressInfoAndConnect(hostname, port) as KX_SOCKET

class NetworkAddress(
    val hostname: String,
    val port: Int
) {
    override fun toString(): String = "NetworkAddress[$hostname:$port]"
}
