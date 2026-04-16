package tech.zhifu.app.myhub.datastore.bootstrap

import tech.zhifu.app.myhub.datastore.model.util.generateUUId
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

class Bootstrap(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
) {
    suspend fun initialize(localeTag: String) {
        val config =
            buildBootstrapConfig(
                userId = generateUUId(),
                localeTag = localeTag,
            )
        val res = userRepository.insertUser(config.user)
        logger.debug { "Bootstrap insertUser res: $res" }
        config.cards.forEach { card ->
            cardRepository.insertCard(
                card = card,
                userId = config.userId,
            )
        }
        logger.debug { "Bootstrap inserted cards count=${config.cards.size}" }
    }
}
