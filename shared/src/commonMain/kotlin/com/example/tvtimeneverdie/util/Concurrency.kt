package com.example.tvtimeneverdie.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Applica [transform] a ogni elemento in parallelo, limitando la concorrenza a [maxConcurrency]. */
suspend fun <T, R> List<T>.mapConcurrently(maxConcurrency: Int, transform: suspend (T) -> R): List<R> {
    val semaphore = Semaphore(maxConcurrency)
    return coroutineScope {
        map { item -> async { semaphore.withPermit { transform(item) } } }.awaitAll()
    }
}
