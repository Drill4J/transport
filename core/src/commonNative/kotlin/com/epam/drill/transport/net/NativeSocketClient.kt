package com.epam.drill.transport.net

import com.epam.drill.transport.exception.*
import io.ktor.utils.io.internal.utils.*
import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import platform.posix.*

class NativeSocketClient(sockfd: KX_SOCKET) : NativeSocket(sockfd) {
    companion object {
        operator fun invoke(networkAddress: NetworkAddress): NativeSocketClient {
            var socket = 0
            val resolve = networkAddress.resolve()
            for (remote in resolve) {
                try {
                    remote.nativeAddress { address, size ->
                        socket = socket(remote.family.convert(), SOCK_STREAM, 0)
                        if (socket < 0) {
                            checkErrors("socket")
                        }
                        val connect = connect(socket, address, size)
                        if (connect != 0) {
                            checkErrors("connect")
                        }
                    }
                } catch (x: Throwable) {
                }

            }
            return NativeSocketClient(socket)
        }
    }

    private var _connected = atomic(false)
    override fun isAlive() = _connected.value
    override fun setIsAlive(isAlive: Boolean) {
        _connected.value = isAlive
    }

    @Suppress("RemoveRedundantCallsOfConversionMethods")
    fun connect() {
        memScoped {
            setNonBlocking()
            _connected.value = true
        }
    }
}
