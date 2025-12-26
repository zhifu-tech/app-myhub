package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.CardDto
import tech.zhifu.app.myhub.datastore.model.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.model.User

/**
 * 远程数据源接口
 * 负责与服务器API通信
 */
interface RemoteCardDataSource {
    suspend fun getAllCards(): List<CardDto>
    suspend fun getCardById(id: String): CardDto?
    suspend fun searchCards(filter: SearchFilter): List<CardDto>
    suspend fun createCard(request: CreateCardRequest): CardDto
    suspend fun updateCard(id: String, request: UpdateCardRequest): CardDto
    suspend fun deleteCard(id: String)
    suspend fun toggleFavorite(id: String): CardDto
}

interface RemoteTagDataSource {
    suspend fun getAllTags(): List<Tag>
    suspend fun getTagById(id: String): Tag?
    suspend fun createTag(tag: Tag): Tag
    suspend fun updateTag(tag: Tag): Tag
    suspend fun deleteTag(id: String)
}

interface RemoteTemplateDataSource {
    suspend fun getAllTemplates(): List<Template>
    suspend fun getTemplateById(id: String): Template?
    suspend fun createTemplate(template: Template): Template
    suspend fun updateTemplate(template: Template): Template
    suspend fun deleteTemplate(id: String)
}

interface RemoteUserDataSource {
    suspend fun getCurrentUser(): User
    suspend fun updateUser(user: User): User
}

interface RemoteStatisticsDataSource {
    suspend fun getStatistics(): Statistics
}

