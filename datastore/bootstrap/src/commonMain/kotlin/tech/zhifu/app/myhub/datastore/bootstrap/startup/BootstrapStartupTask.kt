package tech.zhifu.app.myhub.datastore.bootstrap.startup

import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.startup.StartupTask
import tech.zhifu.app.myhub.startup.StartupTaskIds

internal class BootstrapStartupTask(
    private val userRepository: UserRepository,
    private val bootstrap: Bootstrap,
    private val taskLogger: Logger = logger("BootstrapStartupTask"),
) : StartupTask {
    override val id: String = StartupTaskIds.BOOTSTRAP
    override val critical: Boolean = true
    override val dependencies: Set<String> = emptySet()

    override suspend fun run() {
        if (!userRepository.hasUser()) {
            taskLogger.info { "First launch detected, initializing bootstrap..." }
            bootstrap.initialize("default")
            taskLogger.info { "Bootstrap initialization completed" }
        }
    }
}
