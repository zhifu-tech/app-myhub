package tech.zhifu.app.myhub.datastore.repository.template

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

interface CardTemplateRepository {

    suspend fun insertTemplate(template: CardTemplate, userId: String, needSync: Boolean = true)

    suspend fun getTemplate(templateId: String): TemplateStoreData?

    suspend fun getTemplates(): TemplateStoreData?

    fun streamTemplates(refresh: Boolean = false): Flow<StoreReadResponse<TemplateStoreData>>

    suspend fun fetchTemplates(): Flow<StoreReadResponse<TemplateStoreData>>
}
