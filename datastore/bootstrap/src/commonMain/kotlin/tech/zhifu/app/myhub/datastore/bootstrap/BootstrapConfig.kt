package tech.zhifu.app.myhub.datastore.bootstrap

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

data class BootstrapConfig(
    val userId: String,
    val user: User,
    val userPreferences: UserPreferences,
    val tags: List<Tag>,
    val cards: List<Card>,
)

interface BootstrapConfigBuilder {
    suspend fun buildConfig(
        userId: String,
        localeTag: String,
    ): BootstrapConfig
}
