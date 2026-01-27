package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

interface RemoteCardTemplateDataSource {
    suspend fun getTemplates(type: String? = null): List<CardTemplate>
    suspend fun getTemplateById(id: String): CardTemplate?
    suspend fun createTemplate(template: CardTemplate): CardTemplate
    suspend fun updateTemplate(id: String, template: CardTemplate): CardTemplate
    suspend fun deleteTemplate(id: String)
}
