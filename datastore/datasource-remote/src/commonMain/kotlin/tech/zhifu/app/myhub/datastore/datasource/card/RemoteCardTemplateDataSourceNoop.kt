package tech.zhifu.app.myhub.datastore.datasource.card

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

class RemoteCardTemplateDataSourceNoop : RemoteCardTemplateDataSource {
    override suspend fun getTemplates(type: String?): List<CardTemplate> = emptyList()

    override suspend fun getTemplateById(id: String): CardTemplate? = null

    override suspend fun createTemplate(template: CardTemplate): CardTemplate = template

    override suspend fun updateTemplate(id: String, template: CardTemplate): CardTemplate = template

    override suspend fun deleteTemplate(id: String) = Unit
}
