package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.repository.TemplateRepository
import kotlin.time.Clock

/**
 * 模板仓库实现
 */
class TemplateRepositoryImpl(
    private val localDataSource: LocalTemplateDataSource
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<Template>> {
        return localDataSource.observeTemplates()
    }

    override suspend fun getTemplateById(id: String): Template? {
        return localDataSource.getTemplateById(id)
    }

    override fun getTemplatesByType(type: CardType): Flow<List<Template>> {
        return localDataSource.observeTemplates().map { templates ->
            templates.filter { it.cardType == type }
        }
    }

    override fun getSystemTemplates(): Flow<List<Template>> {
        return localDataSource.observeTemplates().map { templates ->
            templates.filter { it.isSystemTemplate }
        }
    }

    override fun getUserTemplates(): Flow<List<Template>> {
        return localDataSource.observeTemplates().map { templates ->
            templates.filter { !it.isSystemTemplate }
        }
    }

    override suspend fun createTemplate(template: Template): Result<Template> {
        return try {
            localDataSource.insertTemplate(template)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTemplate(template: Template): Result<Template> {
        return try {
            val updated = template.copy(updatedAt = Clock.System.now())
            localDataSource.updateTemplate(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTemplate(id: String): Result<Unit> {
        return try {
            localDataSource.deleteTemplate(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCardFromTemplate(templateId: String): Result<Card> {
        return try {
            val template = localDataSource.getTemplateById(templateId)
                ?: return Result.failure(IllegalStateException("Template not found: $templateId"))

            // 增加模板使用计数
            val updatedTemplate = template.copy(usageCount = template.usageCount + 1)
            localDataSource.updateTemplate(updatedTemplate)

            // 创建卡片
            val now = Clock.System.now()
            val card = Card(
                id = "${now.toEpochMilliseconds()}-${templateId.take(8)}",
                type = template.cardType,
                title = null,
                content = template.defaultContent ?: "",
                author = null,
                source = null,
                language = null,
                tags = template.defaultTags,
                isFavorite = false,
                isTemplate = false,
                createdAt = now,
                updatedAt = now,
                lastReviewedAt = null,
                metadata = template.defaultMetadata
            )

            Result.success(card)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
