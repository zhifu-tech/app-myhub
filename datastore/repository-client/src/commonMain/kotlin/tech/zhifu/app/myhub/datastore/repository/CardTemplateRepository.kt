package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

interface CardTemplateRepository {

    val syncChangeApplier: SyncChangeApplier

    suspend fun insertTemplate(template: CardTemplate, userId: String, needSync: Boolean = true)

    suspend fun getTemplates(): List<CardTemplate>
}
