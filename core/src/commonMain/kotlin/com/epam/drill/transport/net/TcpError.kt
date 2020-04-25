package com.epam.drill.transport.net

data class TcpError(val id: Int, val name: String, val description: String)

expect val errorsMapping: Map<Int, TcpError>

