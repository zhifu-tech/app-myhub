package tech.zhifu.app.myhub.feature.ai.startup

import tech.zhifu.app.myhub.startup.StartupTask
import tech.zhifu.app.myhub.startup.StartupTaskIds

internal class AiBackgroundStartupTask(
    private val service: AiBackgroundMaintenanceService,
) : StartupTask {
    override val id: String = StartupTaskIds.AI_BACKGROUND_MAINTENANCE
    override val critical: Boolean = false
    override val dependencies: Set<String> = setOf(StartupTaskIds.BOOTSTRAP, StartupTaskIds.ANALYTICS)

    override suspend fun run() {
        service.start()
    }
}
