package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template

/**
 * 模板仓库接口（基础接口，同步风格）
 */
interface TemplateRepository {
    suspend fun getAllTemplates(): List<Template>
    suspend fun getTemplateById(id: String): Template?
    suspend fun createTemplate(template: Template): Template
    suspend fun updateTemplate(template: Template): Template
    suspend fun deleteTemplate(id: String): Boolean
}

/**
 * 响应式模板仓库接口（扩展接口，客户端使用）
 */
interface ReactiveTemplateRepository : TemplateRepository {
    fun observeAllTemplates(): Flow<List<Template>>
    fun observeTemplatesByType(type: CardType): Flow<List<Template>>
}

