package com.epam.drill.transport.ws

fun Int.mask(): Int = (1 shl this) - 1
inline fun <T> buildList(callback: ArrayList<T>.() -> Unit): List<T> = arrayListOf<T>().apply(callback)
fun Int.extract(offset: Int, count: Int): Int = (this ushr offset) and count.mask()
fun Int.extract(offset: Int): Boolean = ((this ushr offset) and 1) != 0