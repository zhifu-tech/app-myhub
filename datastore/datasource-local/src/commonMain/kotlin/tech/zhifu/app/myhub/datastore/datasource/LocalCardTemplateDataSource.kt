package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

interface LocalCardTemplateDataSource {
    suspend fun insertTemplate(template: CardTemplate)

    suspend fun updateTemplate(template: CardTemplate)

    suspend fun getTemplate(templateId: String): CardTemplate?

    suspend fun getTemplates(): List<CardTemplate>

    suspend fun getTemplatesByType(type: String): List<CardTemplate>

    suspend fun deleteTemplate(templateId: String)
}
