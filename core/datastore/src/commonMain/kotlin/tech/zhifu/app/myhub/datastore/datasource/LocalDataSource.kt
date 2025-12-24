package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 本地数据源接口
 * 负责本地数据存储（如SQLite、文件存储等）
 */
interface LocalCardDataSource {
    suspend fun getAllCards(): List<Card>
    suspend fun getCardById(id: String): Card?
    suspend fun insertCard(card: Card)
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(id: String)
    suspend fun deleteAllCards()
    fun observeCards(): Flow<List<Card>>
}

interface LocalTagDataSource {
    suspend fun getAllTags(): List<Tag>
    suspend fun getTagById(id: String): Tag?
    suspend fun getTagByName(name: String): Tag?
    suspend fun insertTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(id: String)
    fun observeTags(): Flow<List<Tag>>
}

interface LocalTemplateDataSource {
    suspend fun getAllTemplates(): List<Template>
    suspend fun getTemplateById(id: String): Template?
    suspend fun insertTemplate(template: Template)
    suspend fun updateTemplate(template: Template)
    suspend fun deleteTemplate(id: String)
    fun observeTemplates(): Flow<List<Template>>
}

interface LocalUserDataSource {
    suspend fun getCurrentUser(): User?
    suspend fun saveUser(user: User)
    suspend fun clearUser()
    fun observeUser(): Flow<User?>
}

interface LocalStatisticsDataSource {
    suspend fun getStatistics(): Statistics?
    suspend fun saveStatistics(statistics: Statistics)
    suspend fun clearStatistics()
}

