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
package com.epam.drill.transport.ws

import com.epam.drill.transport.exception.*
import com.epam.drill.transport.lang.*
import com.epam.drill.transport.net.*
import com.epam.drill.transport.stream.*
import kotlinx.coroutines.*
import kotlin.test.*

suspend fun AsyncStream.readWsFrame(): WsFrame {
    val asyncStream = this
    val b0 = asyncStream.readU8()
    val b1 = asyncStream.readU8()
    val isFinal = b0.extract(7)
    val opcode = WsOpcode(b0.extract(0, 4))
    val frameIsBinary = when (opcode) {
        WsOpcode.Text -> false
        WsOpcode.Binary -> true
        else -> false
    }

    val partialLength = b1.extract(0, 7)
    val isMasked = b1.extract(7)

    val length = when (partialLength) {
        126 -> asyncStream.readU16BE()
        127 -> {
            val tmp = asyncStream.readS32BE()
            if (tmp != 0) throw WsException("message too long")
            asyncStream.readS32BE()
        }
        else -> partialLength
    }
    if (length == 0 && b0 == 0) fail("can't read empty payload!")
    val mask = if (isMasked) asyncStream.readBytesExact(4) else null
    val unmaskedData = readExactBytes(length)
    val finalData = WsFrame.applyMask(unmaskedData, mask)
    return WsFrame(finalData, opcode, isFinal, frameIsBinary)
}

private suspend fun AsyncStream.readExactBytes(length: Int): ByteArray {
    val byteArray = ByteArray(length)
    var remaining = length
    var coffset = 0
    val reader = this
    while (remaining > 0) {
        val read = reader.read(byteArray, coffset, remaining)
        if (read < 0) break
        if (read == 0) throw EOFException("Not enough data. Expected=$length, Read=${length - remaining}, Remaining=$remaining")
        coffset += read
        remaining -= read
    }
    return byteArray
}

@SharedImmutable
private val sendContext = newSingleThreadContext("send dispatcher")

suspend fun AsyncStream.sendWsFrame(frame: WsFrame) = withContext(sendContext) {
    writeBytes(frame.toByteArray())
}