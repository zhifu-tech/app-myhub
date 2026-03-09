package tech.zhifu.app.myhub.startup

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import kotlin.random.Random
import kotlin.time.TimeSource

class StartupOrchestrator(
    private val appStartupScope: AppStartupScope,
    private val tasks: List<StartupTask>,
    private val logger: Logger = logger("StartupOrchestrator"),
    private val criticalRetryPolicy: StartupRetryPolicy = StartupRetryPolicy(),
    private val deferredRetryPolicy: StartupRetryPolicy = StartupRetryPolicy(),
    private val random: Random = Random.Default,
) {
    private val startupTasks = tasks.toMutableList()
    private var isCleanedUp = false

    fun start() {
        appStartupScope.launch {
            runCatching {
                run()
            }.onFailure { error ->
                logger.error(error) { "Startup orchestration failed" }
            }
        }
    }

    suspend fun run() {
        try {
            if (startupTasks.isEmpty()) return

            val taskSnapshot = startupTasks.toList()
            val taskById = taskSnapshot.associateBy { it.id }
            validateDependencies(taskSnapshot, taskById)

            val criticalTasks = taskSnapshot.filter { it.critical }
            val deferredTasks = taskSnapshot.filterNot { it.critical }

            for (task in topologicalSort(criticalTasks, taskById)) {
                runTask(task)
            }

            runDeferred(taskSnapshot, deferredTasks, taskById)
        } finally {
            cleanup()
        }
    }

    private fun validateDependencies(
        allTasks: List<StartupTask>,
        taskById: Map<String, StartupTask>
    ) {
        allTasks.forEach { task ->
            task.dependencies.forEach { dependency ->
                require(taskById.containsKey(dependency)) {
                    "Startup task '${task.id}' depends on missing task '$dependency'"
                }
            }
        }
    }

    private suspend fun runDeferred(
        allTasks: List<StartupTask>,
        deferredTasks: List<StartupTask>,
        taskById: Map<String, StartupTask>
    ) {
        if (deferredTasks.isEmpty()) return

        val deferredIds = deferredTasks.map { it.id }.toSet()
        val allDone = allTasks.filter { it.critical }.map { it.id }.toMutableSet()
        val pending = deferredTasks.associateBy { it.id }.toMutableMap()

        while (pending.isNotEmpty()) {
            val ready = pending.values.filter { task ->
                task.dependencies.all { dependency ->
                    dependency !in deferredIds || dependency in allDone
                }
            }

            if (ready.isEmpty()) {
                val cyclicIds = pending.keys.sorted().joinToString(", ")
                error("Cyclic or blocked deferred startup dependencies: $cyclicIds")
            }

            supervisorScope {
                ready.map { task ->
                    async { runTask(task) }
                }.awaitAll()
            }

            ready.forEach { task ->
                allDone.add(task.id)
                pending.remove(task.id)
            }
        }
    }

    private fun topologicalSort(
        orderedTasks: List<StartupTask>,
        taskById: Map<String, StartupTask>
    ): List<StartupTask> {
        val taskIds = orderedTasks.map { it.id }.toSet()
        val inDegree = orderedTasks.associate { task ->
            task.id to task.dependencies.count { dependency -> dependency in taskIds }
        }.toMutableMap()
        val queue = ArrayDeque<StartupTask>()
        orderedTasks.forEach { task ->
            if (inDegree.getValue(task.id) == 0) queue.add(task)
        }

        val sorted = mutableListOf<StartupTask>()
        while (queue.isNotEmpty()) {
            val task = queue.removeFirst()
            sorted.add(task)

            orderedTasks.forEach { candidate ->
                if (task.id in candidate.dependencies && inDegree.getValue(candidate.id) > 0) {
                    val nextInDegree = inDegree.getValue(candidate.id) - 1
                    inDegree[candidate.id] = nextInDegree
                    if (nextInDegree == 0) queue.add(candidate)
                }
            }
        }

        check(sorted.size == orderedTasks.size) {
            val cyclicIds = orderedTasks.map { it.id }.filterNot { id -> sorted.any { it.id == id } }
            "Cyclic critical startup dependencies: ${cyclicIds.joinToString(", ")}"
        }

        sorted.forEach { task ->
            task.dependencies.forEach { dependency ->
                require(taskById.containsKey(dependency)) {
                    "Startup task '${task.id}' depends on missing task '$dependency'"
                }
            }
        }

        return sorted
    }

    private suspend fun runTask(task: StartupTask) {
        val retryPolicy = if (task.critical) criticalRetryPolicy else deferredRetryPolicy
        val startNanos = TimeSource.Monotonic.markNow()
        var attempt = 1
        var lastError: Throwable? = null

        while (attempt <= retryPolicy.maxAttempts.coerceAtLeast(1)) {
            runCatching {
                task.run()
            }.onSuccess {
                logger.info {
                    "Startup task completed: ${task.id}, attempts=$attempt, elapsed=${startNanos.elapsedNow()}"
                }
                return
            }.onFailure { error ->
                lastError = error
                val canRetry = attempt < retryPolicy.maxAttempts && isRetryableStartupError(error)
                if (canRetry) {
                    val nextDelay = retryPolicy.nextDelayMs(attempt, random)
                    logger.warn(error) {
                        "Startup task failed, retrying: ${task.id}, attempt=$attempt/${retryPolicy.maxAttempts}, nextDelayMs=$nextDelay"
                    }
                    delay(nextDelay)
                } else {
                    break
                }
            }

            attempt += 1
        }

        val error = lastError ?: IllegalStateException("Startup task failed without error: ${task.id}")
        logger.error(error) { "Startup task failed: ${task.id}, attempts=${retryPolicy.maxAttempts}" }
        if (task.critical) {
            throw error
        }
    }

    private fun cleanup() {
        if (isCleanedUp) return
        isCleanedUp = true
        startupTasks.clear()
        appStartupScope.close()
    }
}
