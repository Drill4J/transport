package com.epam.drill.transport.net

import io.ktor.utils.io.internal.utils.KX_SOCKET

class NativeSocketServer(val sockfds: KX_SOCKET) : NativeSocket(sockfds) {

    private var isRunning = true
    override fun isAlive() = isRunning
    override fun setIsAlive(isAlive: Boolean) {
        isRunning = isAlive
    }
}