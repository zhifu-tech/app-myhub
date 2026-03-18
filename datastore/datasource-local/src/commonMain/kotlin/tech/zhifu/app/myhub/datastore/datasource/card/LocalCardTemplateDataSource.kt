package tech.zhifu.app.myhub.datastore.datasource.card

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

interface LocalCardTemplateDataSource {
    suspend fun insertTemplate(template: CardTemplate)

    suspend fun updateTemplate(template: CardTemplate)

    suspend fun getTemplate(templateId: String): CardTemplate?

    suspend fun getTemplates(): List<CardTemplate>

    /** 可持续观察模板列表变化，供 Store SourceOfTruth 使用。 */
    fun observeTemplates(): Flow<List<CardTemplate>>

    suspend fun getTemplatesByType(type: String): List<CardTemplate>

    suspend fun deleteTemplate(templateId: String)
}
