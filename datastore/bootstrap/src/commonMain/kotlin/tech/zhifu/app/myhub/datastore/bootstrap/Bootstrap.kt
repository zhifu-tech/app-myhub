package tech.zhifu.app.myhub.datastore.bootstrap

import tech.zhifu.app.myhub.datastore.model.util.generateUUId
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

class Bootstrap(
    private val userRepository: UserRepository,
) {
    suspend fun initialize(localeTag: String) {
        val config = buildBootstrapConfig(
            userId = generateUUId(),
            localeTag = localeTag,
        )
        val res = userRepository.insertUser(config.user)
        logger.debug { "Bootstrap insertUser res: $res" }
    }
}
