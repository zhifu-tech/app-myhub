package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template

/**
 * 模板仓库接口
 */
interface TemplateRepository {
    /**
     * 获取所有模板
     */
    fun getAllTemplates(): Flow<List<Template>>

    /**
     * 根据ID获取模板
     */
    suspend fun getTemplateById(id: String): Template?

    /**
     * 根据类型获取模板
     */
    fun getTemplatesByType(type: CardType): Flow<List<Template>>

    /**
     * 获取系统模板
     */
    fun getSystemTemplates(): Flow<List<Template>>

    /**
     * 获取用户自定义模板
     */
    fun getUserTemplates(): Flow<List<Template>>

    /**
     * 创建新模板
     */
    suspend fun createTemplate(template: Template): Result<Template>

    /**
     * 更新模板
     */
    suspend fun updateTemplate(template: Template): Result<Template>

    /**
     * 删除模板
     */
    suspend fun deleteTemplate(id: String): Result<Unit>

    /**
     * 使用模板创建卡片
     */
    suspend fun createCardFromTemplate(templateId: String): Result<Card>
}

