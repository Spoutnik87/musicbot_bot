package fr.spoutnik87.util

import kotlinx.coroutines.delay

suspend fun <T> retry(count: Int, block: suspend () -> T): T {
    for (i in 1..count) {
        try {
            delay(5000)
            return block()
        } catch (e: Exception) {
        }
    }
    return block()
}