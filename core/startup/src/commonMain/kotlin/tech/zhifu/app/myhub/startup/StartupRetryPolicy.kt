package tech.zhifu.app.myhub.startup

import kotlin.random.Random

data class StartupRetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 200,
    val maxDelayMs: Long = 3_000,
    val backoffFactor: Double = 2.0,
    val jitterRatio: Double = 0.2,
)

internal fun StartupRetryPolicy.nextDelayMs(attempt: Int, random: Random): Long {
    require(attempt >= 1) { "attempt must be >= 1" }
    val pureDelay = (initialDelayMs * backoffFactor.pow(attempt - 1))
        .toLong()
        .coerceAtMost(maxDelayMs)
        .coerceAtLeast(0)
    if (pureDelay == 0L || jitterRatio <= 0.0) return pureDelay

    val jitter = (pureDelay * jitterRatio).toLong().coerceAtLeast(1)
    val minDelay = (pureDelay - jitter).coerceAtLeast(0)
    val maxDelay = pureDelay + jitter
    return random.nextLong(minDelay, maxDelay + 1)
}

private fun Double.pow(power: Int): Double {
    var result = 1.0
    repeat(power) { result *= this }
    return result
}

internal fun isRetryableStartupError(error: Throwable): Boolean {
    return when (error) {
        is kotlinx.coroutines.CancellationException -> false
        is IllegalArgumentException -> false
        is IllegalStateException -> false
        is NoSuchElementException -> false
        else -> true
    }
}

