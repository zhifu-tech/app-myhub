package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
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

    override suspend fun getTemplates(): List<CardTemplate> {
        return database.card_templateQueries
            .selectAllCardTemplates()
            .awaitAsList()
            .map(DbCardTemplate::toDomain)
    }
}
