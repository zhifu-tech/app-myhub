package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.repository.CardTemplateRepository

/**
 * 卡片模板仓库实现（服务端）
 * 使用 LocalCardTemplateDataSource 实现，避免代码重复
 */
class CardTemplateRepositoryImpl(
    private val localDataSource: LocalCardTemplateDataSource
) : CardTemplateRepository {

    override suspend fun getTemplates(): List<CardTemplate> {
        return localDataSource.getTemplates()
    }

    override suspend fun getTemplateById(templateId: String): CardTemplate? {
        return localDataSource.getTemplate(templateId)
    }

    override suspend fun getTemplatesByType(type: String): List<CardTemplate> {
        return localDataSource.getTemplatesByType(type)
    }

    override suspend fun createTemplate(template: CardTemplate): CardTemplate {
        try {
            localDataSource.insertTemplate(template)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create template: ${e.message}", e)
        }
        return template
    }

    override suspend fun updateTemplate(template: CardTemplate): CardTemplate {
        // 检查模板是否存在
        val existing = localDataSource.getTemplate(template.id)
            ?: throw IllegalArgumentException("Template with id '${template.id}' not found")

        try {
            localDataSource.updateTemplate(template)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to update template: ${e.message}", e)
        }
        return template
    }

    override suspend fun deleteTemplate(templateId: String) {
        localDataSource.deleteTemplate(templateId)
    }
}
