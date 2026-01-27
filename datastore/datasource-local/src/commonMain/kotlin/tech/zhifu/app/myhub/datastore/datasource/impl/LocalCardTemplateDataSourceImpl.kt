package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.database.Card_template as DbCardTemplate

class LocalCardTemplateDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCardTemplateDataSource {

    override suspend fun insertTemplate(template: CardTemplate) {
        database.card_templateQueries.insertCardTemplate(
            id = template.id,
            type = template.type,
            title = template.title,
            content = template.content,
            description = template.description,
            created_at = template.createdAt.toString()
        )
    }

    override suspend fun updateTemplate(template: CardTemplate) {
        database.card_templateQueries.updateCardTemplate(
            type = template.type,
            title = template.title,
            content = template.content,
            description = template.description,
            id = template.id
        )
    }

    override suspend fun getTemplate(templateId: String): CardTemplate? {
        return database.card_templateQueries
            .selectCardTemplateById(templateId)
            .awaitAsOneOrNull()
            ?.toDomain()
    }

    override suspend fun getTemplates(): List<CardTemplate> {
        return database.card_templateQueries
            .selectAllCardTemplates()
            .awaitAsList()
            .map(DbCardTemplate::toDomain)
    }

    override suspend fun getTemplatesByType(type: String): List<CardTemplate> {
        return database.card_templateQueries
            .selectCardTemplatesByType(type)
            .awaitAsList()
            .map(DbCardTemplate::toDomain)
    }

    override suspend fun deleteTemplate(templateId: String) {
        database.card_templateQueries.deleteCardTemplate(templateId)
    }
}
