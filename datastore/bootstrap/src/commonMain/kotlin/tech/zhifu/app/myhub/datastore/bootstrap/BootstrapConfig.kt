package tech.zhifu.app.myhub.datastore.bootstrap

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

data class BootstrapConfig(
    val userId: String,
    val user: User,
    val userPreferences: UserPreferences,
    val tags: List<Tag>,
    val collections: List<Collection>,
    val cards: List<Card>,
    val templates: List<CardTemplate>,
)

interface BootstrapConfigBuilder {
    suspend fun buildConfig(
        userId: String,
        localeTag: String,
    ): BootstrapConfig
}
