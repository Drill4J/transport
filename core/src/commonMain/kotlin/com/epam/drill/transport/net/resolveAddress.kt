package com.epam.drill.transport.net

import com.epam.drill.transport.exception.*

expect fun resolveAddress(host: String, port: Int): Any

expect fun getAvailableBytes(sockRaw: ULong): Int

expect fun close(sockRaw: ULong)

expect fun setSocketBlocking(sockRaw: ULong, is_blocking: Boolean)

expect fun getError(): Int

expect val EAGAIN_ERROR: Int

fun isAllowedSocketError(): Boolean {
    val socketError = getError()
    return socketError == EAGAIN_ERROR || socketError == 316 || socketError == 0
}

fun checkErrors(name: String) {
    val error = getError()
    if (error != 0) {
        throwError(name, error)
    }
}

fun throwError(name: String, code: Int) {
    throw WsException("error($name): ${errorsMapping[code]?.description ?: "unknown error($code)"}")
}