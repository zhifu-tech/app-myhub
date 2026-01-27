package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate

/**
 * 卡片模板仓库接口（服务端）
 */
interface CardTemplateRepository {
    /**
     * 获取所有模板
     */
    suspend fun getTemplates(): List<CardTemplate>

    /**
     * 根据 ID 获取模板
     */
    suspend fun getTemplateById(templateId: String): CardTemplate?

    /**
     * 根据类型获取模板
     */
    suspend fun getTemplatesByType(type: String): List<CardTemplate>

    /**
     * 创建模板
     */
    suspend fun createTemplate(template: CardTemplate): CardTemplate

    /**
     * 更新模板
     */
    suspend fun updateTemplate(template: CardTemplate): CardTemplate

    /**
     * 删除模板
     */
    suspend fun deleteTemplate(templateId: String)
}
