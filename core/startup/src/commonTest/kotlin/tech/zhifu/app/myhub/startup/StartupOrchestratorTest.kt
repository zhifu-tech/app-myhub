package tech.zhifu.app.myhub.startup

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StartupOrchestratorTest {

    @Test
    fun `critical tasks should run in dependency order`() {
        runBlocking {
            val events = mutableListOf<String>()
            val taskA = TestTask(
                id = "A",
                critical = true,
                dependencies = emptySet(),
            ) { events += "A" }
            val taskB = TestTask(
                id = "B",
                critical = true,
                dependencies = setOf("A"),
            ) { events += "B" }
            val taskC = TestTask(
                id = "C",
                critical = true,
                dependencies = setOf("B"),
            ) { events += "C" }

            StartupOrchestrator(
                appStartupScope = AppStartupScope(),
                tasks = listOf(taskC, taskA, taskB)
            ).run()
            assertContentEquals(
                expected = listOf("A", "B", "C"),
                actual = events
            )
        }
    }

    @Test
    fun `cyclic critical dependency should throw`() {
        runBlocking {
            val taskA = TestTask(
                id = "A",
                critical = true,
                dependencies = setOf("B"),
            ) {}
            val taskB = TestTask(
                id = "B",
                critical = true,
                dependencies = setOf("A"),
            ) {}

            assertFailsWith<IllegalStateException> {
                StartupOrchestrator(
                    appStartupScope = AppStartupScope(),
                    tasks = listOf(taskA, taskB)
                ).run()
            }
        }
    }

    @Test
    fun `critical task failure should stop startup`() {
        runBlocking {
            val events = mutableListOf<String>()
            val criticalFail = TestTask(
                id = "criticalFail",
                critical = true,
                dependencies = emptySet(),
            ) {
                events += "criticalFail"
                error("boom")
            }
            val deferred = TestTask(
                id = "deferred",
                critical = false,
                dependencies = emptySet(),
            ) { events += "deferred" }

            assertFailsWith<IllegalStateException> {
                StartupOrchestrator(
                    appStartupScope = AppStartupScope(),
                    tasks = listOf(criticalFail, deferred)
                ).run()
            }
            assertTrue("deferred" !in events)
        }
    }

    @Test
    fun `deferred failure should not block other deferred tasks`() {
        runBlocking {
            val events = mutableListOf<String>()
            val critical = TestTask(
                id = "critical",
                critical = true,
                dependencies = emptySet(),
            ) { events += "critical" }
            val deferredFail = TestTask(
                id = "deferredFail",
                critical = false,
                dependencies = emptySet(),
            ) {
                events += "deferredFail"
                error("boom")
            }
            val deferredOk = TestTask(
                id = "deferredOk",
                critical = false,
                dependencies = emptySet(),
            ) { events += "deferredOk" }

            StartupOrchestrator(
                appStartupScope = AppStartupScope(),
                tasks = listOf(critical, deferredFail, deferredOk),
            ).run()

            assertTrue("critical" in events)
            assertTrue("deferredFail" in events)
            assertTrue("deferredOk" in events)
        }
    }

    @Test
    fun `critical task should retry and eventually succeed`() {
        runBlocking {
            var attempts = 0
            val retryTask = TestTask(
                id = "retryCritical",
                critical = true,
                dependencies = emptySet(),
            ) {
                attempts += 1
                if (attempts < 3) throw RuntimeException("transient")
            }

            StartupOrchestrator(
                appStartupScope = AppStartupScope(),
                tasks = listOf(retryTask),
                criticalRetryPolicy = StartupRetryPolicy(
                    maxAttempts = 3,
                    initialDelayMs = 0,
                    maxDelayMs = 0,
                    jitterRatio = 0.0,
                ),
                deferredRetryPolicy = StartupRetryPolicy(
                    maxAttempts = 1,
                    initialDelayMs = 0,
                    maxDelayMs = 0,
                    jitterRatio = 0.0,
                ),
            ).run()

            assertEquals(3, attempts)
        }
    }

    @Test
    fun `deferred task should stop retrying after max attempts`() {
        runBlocking {
            var attempts = 0
            val critical = TestTask(
                id = "critical",
                critical = true,
                dependencies = emptySet(),
            ) {}
            val deferred = TestTask(
                id = "deferredRetry",
                critical = false,
                dependencies = emptySet(),
            ) {
                attempts += 1
                throw RuntimeException("transient")
            }

            StartupOrchestrator(
                appStartupScope = AppStartupScope(),
                tasks = listOf(critical, deferred),
                criticalRetryPolicy = StartupRetryPolicy(
                    maxAttempts = 1,
                    initialDelayMs = 0,
                    maxDelayMs = 0,
                    jitterRatio = 0.0,
                ),
                deferredRetryPolicy = StartupRetryPolicy(
                    maxAttempts = 3,
                    initialDelayMs = 0,
                    maxDelayMs = 0,
                    jitterRatio = 0.0,
                ),
            ).run()

            assertEquals(3, attempts)
        }
    }

    @Test
    fun `critical non-retryable error should fail fast`() {
        runBlocking {
            var attempts = 0
            val critical = TestTask(
                id = "criticalNonRetryable",
                critical = true,
                dependencies = emptySet(),
            ) {
                attempts += 1
                throw IllegalArgumentException("invalid config")
            }

            assertFailsWith<IllegalArgumentException> {
                StartupOrchestrator(
                    appStartupScope = AppStartupScope(),
                    tasks = listOf(critical),
                    criticalRetryPolicy = StartupRetryPolicy(
                        maxAttempts = 3,
                        initialDelayMs = 0,
                        maxDelayMs = 0,
                        jitterRatio = 0.0,
                    ),
                ).run()
            }

            assertEquals(1, attempts)
        }
    }
}

private class TestTask(
    override val id: String,
    override val critical: Boolean,
    override val dependencies: Set<String>,
    private val block: suspend () -> Unit,
) : StartupTask {
    override suspend fun run() = block()
}
