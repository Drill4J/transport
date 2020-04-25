package com.epam.drill.transport.net

expect fun resolveAddress(host: String, port: Int): Any

expect fun getAvailableBytes(sockRaw: ULong): Int

expect fun close(sockRaw: ULong)

expect fun setSocketNonBlocking(sockRaw: ULong)

expect fun getError(): Int

expect val EAGAIN_ERROR: Int

fun isAllowedSocketError(): Boolean {
    val socketError = getError()
    return socketError == EAGAIN_ERROR || socketError == 316 || socketError == 0
}

fun checkErrors(name: String) {
    val error = getError()
    if (error != 0) {
        error("error($name): ${errorsMapping[error]?.description ?: "unknown error"}")
    }
}