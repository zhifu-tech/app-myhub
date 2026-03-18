package tech.zhifu.app.myhub.datastore.bootstrap

import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import kotlin.random.Random

class Bootstrap(
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val cardRepository: CardRepository,
    private val configBuilder: () -> BootstrapConfigBuilder,
) {
    suspend fun initialize(localeTag: String) {
        logger.debug { "Initializing bootstrap with locale tag: $localeTag" }
        val config = configBuilder().buildConfig(
            userId = generateUUId(),
            localeTag = localeTag,
        )
        userRepository.insertUser(config.user)
        logger.debug { "Inserted user: ${config.user}" }
        userRepository.insertUserPreferences(config.userPreferences)
        logger.debug { "Inserted user preferences: ${config.userPreferences}" }
        config.tags.forEach {
            tagRepository.insertTag(it)
        }
        logger.debug { "Inserted tags: ${config.tags}" }
        config.cards.forEach {
            cardRepository.insertCard(it, needSync = false)
        }
        logger.debug { "Inserted cards: ${config.cards}" }
    }
}


private fun generateUUId(): String {
    val bytes = ByteArray(16)
    Random.nextBytes(bytes)
    // RFC 4122 version 4
    bytes[6] = ((bytes[6].toInt() and 0x0F) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3F) or 0x80).toByte()
    return bytes.toUuidString()
}

private fun ByteArray.toUuidString(): String {
    val hexChars = "0123456789abcdef"
    val out = StringBuilder(36)
    forEachIndexed { index, byte ->
        val value = byte.toInt() and 0xFF
        out.append(hexChars[value ushr 4])
        out.append(hexChars[value and 0x0F])
        when (index) {
            3, 5, 7, 9 -> out.append('-')
        }
    }
    return out.toString()
}
