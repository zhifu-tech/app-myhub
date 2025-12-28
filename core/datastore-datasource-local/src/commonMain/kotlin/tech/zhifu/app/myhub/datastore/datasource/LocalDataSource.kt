package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.model.User

/**
 * 本地数据源接口
 * 负责本地数据存储（如SQLite、文件存储等）
 */
interface LocalCardDataSource {
    suspend fun getAllCards(userId: String): List<Card>
    suspend fun getCardById(id: String, userId: String): Card?
    suspend fun insertCard(card: Card, userId: String)
    suspend fun updateCard(card: Card, userId: String)
    suspend fun deleteCard(id: String, userId: String)
    suspend fun deleteAllCards(userId: String)
    fun observeCards(userId: String): Flow<List<Card>>
}

interface LocalTagDataSource {
    suspend fun getAllTags(userId: String): List<Tag>
    suspend fun getTagById(id: String, userId: String): Tag?
    suspend fun getTagByName(name: String, userId: String): Tag?
    suspend fun insertTag(tag: Tag, userId: String)
    suspend fun updateTag(tag: Tag, userId: String)
    suspend fun deleteTag(id: String, userId: String)
    fun observeTags(userId: String): Flow<List<Tag>>
}

interface LocalTemplateDataSource {
    suspend fun getAllTemplates(userId: String): List<Template>
    suspend fun getTemplateById(id: String, userId: String): Template?
    suspend fun insertTemplate(template: Template, userId: String)
    suspend fun updateTemplate(template: Template, userId: String)
    suspend fun deleteTemplate(id: String, userId: String)
    fun observeTemplates(userId: String): Flow<List<Template>>
}

interface LocalUserDataSource {
    suspend fun getCurrentUser(): User?
    suspend fun saveUser(user: User)
    suspend fun clearUser()
    fun observeUser(): Flow<User?>
}

interface LocalStatisticsDataSource {
    suspend fun getStatistics(userId: String): Statistics?
    suspend fun saveStatistics(statistics: Statistics, userId: String)
    suspend fun clearStatistics(userId: String)
}

