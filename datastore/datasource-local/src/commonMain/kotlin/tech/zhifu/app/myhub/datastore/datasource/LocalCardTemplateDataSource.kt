package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

interface LocalCardTemplateDataSource {

    suspend fun insertTemplate(template: CardTemplate)

    suspend fun getTemplates(): List<CardTemplate>
}
