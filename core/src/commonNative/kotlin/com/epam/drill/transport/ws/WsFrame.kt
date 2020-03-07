package com.epam.drill.transport.ws

import com.epam.drill.transport.stream.*
import kotlin.random.Random

@Suppress("ConstantConditionIf")
class WsFrame(val data: ByteArray, val type: WsOpcode, val isFinal: Boolean = true, val frameIsBinary: Boolean = true) {
    fun toByteArray(): ByteArray = MemorySyncStreamToByteArray {
        val isMasked = false
        val mask = Random.nextBytes(4)
        val sizeMask = (0x00)

        write8(type.id or (if (isFinal) 0x80 else 0x00))

        when {
            data.size < 126 -> write8(data.size or sizeMask)
            data.size < 65536 -> {
                write8(126 or sizeMask)
                write16BE(data.size)
            }
            else -> {
                write8(127 or sizeMask)
                write32BE(0)
                write32BE(data.size)
            }
        }

        if (isMasked) writeBytes(mask)

        writeBytes(
            if (isMasked) applyMask(
                data,
                mask
            ) else data
        )
    }

    companion object {
        fun applyMask(payload: ByteArray, mask: ByteArray?): ByteArray {
            if (mask == null) return payload
            val maskedPayload = ByteArray(payload.size)
            for (n in payload.indices) maskedPayload[n] =
                (payload[n].toInt() xor mask[n % mask.size].toInt()).toByte()
            return maskedPayload
        }
    }
}