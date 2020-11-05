package com.epam.drill.transport.net

import com.epam.drill.logger.Logging
import io.ktor.utils.io.internal.utils.*
import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val logger = Logging.logger("NativeSocketClient")


/*
    List<addrinfo> listAddr = getaddrinfo(host=argv[1], DEFAULT_PORT, &hints, &result); //done
    for(addrinfo addrInfo : listAddr) {
        if(connect.isGood){
            break;
        }
    }
    //freeaddrinfo

 */
class NativeSocketClient(sockfd: KX_SOCKET) : NativeSocket(sockfd) {
    companion object {
        operator fun invoke(networkAddress: NetworkAddress): NativeSocketClient {
            logger.debug { "invoke NativeSocketClient" }
            val socket = networkAddress.resolve()
            logger.debug { "resolve $socket" }
            return NativeSocketClient(socket.convert())
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
            logger.debug { "connect NativeSocketClient " }
            setNonBlocking()
        }
    }
}
