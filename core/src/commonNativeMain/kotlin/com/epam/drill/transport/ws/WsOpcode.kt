package com.epam.drill.transport.ws

inline class WsOpcode(val id: Int) {

    companion object {
        val Text = WsOpcode(0x01)
        val Binary = WsOpcode(0x02)
        val Close = WsOpcode(0x08)
        val Ping = WsOpcode(0x09)
        val Pong = WsOpcode(0x0A)
    }
}