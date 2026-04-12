package com.wa.toolkit.xposed.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object TimeoutUtil {

    private val scheduler: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)

    /**
     * Adds a timeout to a CompletableFuture
     * @param future The original CompletableFuture
     * @param timeout Timeout duration
     * @param unit Time unit
     * @param <T> Type of the result
     * @return New CompletableFuture with timeout
    </T> */
    @JvmStatic
    fun <T> withTimeout(future: CompletableFuture<T>, timeout: Long, unit: TimeUnit): CompletableFuture<T> {
        val timeoutFuture = CompletableFuture<T>()

        // Schedules a task to complete the future with an exception after the timeout
        scheduler.schedule({
            timeoutFuture.completeExceptionally(
                TimeoutException("Operation exceeded the time limit of $timeout $unit")
            )
        }, timeout, unit)

        // Returns the first to complete (either the original or the timeout)
        return CompletableFuture.anyOf(future, timeoutFuture)
            .thenApply { result -> result as T }
            .exceptionally { ex ->
                // Cancels the original future if a timeout occurs
                future.cancel(true)
                throw RuntimeException(ex)
            }
    }
}
