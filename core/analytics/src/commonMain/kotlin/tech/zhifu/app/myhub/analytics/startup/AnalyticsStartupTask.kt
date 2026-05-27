package tech.zhifu.app.myhub.analytics.startup

import tech.zhifu.app.myhub.analytics.AnalyticsManager
import tech.zhifu.app.myhub.startup.StartupTask
import tech.zhifu.app.myhub.startup.StartupTaskIds

internal class AnalyticsStartupTask(
    private val analyticsManager: AnalyticsManager,
) : StartupTask {
    override val id: String = StartupTaskIds.ANALYTICS
    override val critical: Boolean = false
    override val dependencies: Set<String> = setOf(StartupTaskIds.BOOTSTRAP)

    override suspend fun run() {
        analyticsManager.initialize()
    }
}
