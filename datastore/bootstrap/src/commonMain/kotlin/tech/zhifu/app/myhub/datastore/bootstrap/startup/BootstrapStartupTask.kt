package tech.zhifu.app.myhub.datastore.bootstrap.startup

import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.startup.StartupTask
import tech.zhifu.app.myhub.startup.StartupTaskIds

internal class BootstrapStartupTask(
    private val userRepository: UserRepository,
    private val bootstrap: Bootstrap,
) : StartupTask {
    override val id: String = StartupTaskIds.BOOTSTRAP
    override val critical: Boolean = true
    override val dependencies: Set<String> = emptySet()

    override suspend fun run() {
        val logger = logger("BootstrapStartupTask")
        val hasUser = userRepository.hasUser()
        if (!hasUser) {
            logger.info { "First launch detected, initializing bootstrap..." }
            bootstrap.initialize("default")
            logger.info { "Bootstrap initialization completed" }
        } else {
            logger.info { "Bootstrap skipped: existing user detected." }
        }
    }
}
