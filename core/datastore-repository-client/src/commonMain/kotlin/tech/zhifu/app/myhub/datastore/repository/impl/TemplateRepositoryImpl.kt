package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.repository.ReactiveTemplateRepository
import kotlin.time.Clock

/**
 * 模板仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class TemplateRepositoryImpl(
    private val localDataSource: LocalTemplateDataSource,
    private val remoteDataSource: RemoteTemplateDataSource
) : ReactiveTemplateRepository {

    override suspend fun getAllTemplates(): List<Template> {
        return localDataSource.getAllTemplates()
    }

    override fun observeAllTemplates(): Flow<List<Template>> {
        return localDataSource.observeTemplates()
    }

    override fun observeTemplatesByType(type: CardType): Flow<List<Template>> {
        return localDataSource.observeTemplates().map { templates ->
            templates.filter { it.cardType == type }
        }
    }

    override suspend fun getTemplateById(id: String): Template? {
        // 先从本地获取
        val localTemplate = localDataSource.getTemplateById(id)
        if (localTemplate != null) {
            return localTemplate
        }

        // 如果本地没有，从远程获取
        return try {
            val remoteTemplate = remoteDataSource.getTemplateById(id)
            remoteTemplate?.let { localDataSource.insertTemplate(it) }
            remoteTemplate
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun createTemplate(template: Template): Template {
        // 先保存到本地
        localDataSource.insertTemplate(template)

        // 然后同步到远程
        return try {
            val remoteTemplate = remoteDataSource.createTemplate(template)
            localDataSource.updateTemplate(remoteTemplate)
            remoteTemplate
        } catch (_: Exception) {
            // 如果远程同步失败，返回本地模板
            template
        }
    }

    override suspend fun updateTemplate(template: Template): Template {
        // 先更新本地
        val updated = template.copy(updatedAt = Clock.System.now())
        localDataSource.updateTemplate(updated)

        // 然后同步到远程
        return try {
            val remoteTemplate = remoteDataSource.updateTemplate(updated)
            localDataSource.updateTemplate(remoteTemplate)
            remoteTemplate
        } catch (_: Exception) {
            // 如果远程同步失败，返回本地模板
            updated
        }
    }

    override suspend fun deleteTemplate(id: String): Boolean {
        return try {
            // 先删除本地
            localDataSource.deleteTemplate(id)

            // 然后删除远程
            try {
                remoteDataSource.deleteTemplate(id)
            } catch (_: Exception) {
                // 如果远程删除失败，仍然返回成功（本地已删除）
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 从模板创建卡片（客户端特有方法）
     */
    suspend fun createCardFromTemplate(templateId: String): Card {
        val template = getTemplateById(templateId)
            ?: throw IllegalStateException("Template not found: $templateId")

        // 增加模板使用计数
        val updatedTemplate = template.copy(usageCount = template.usageCount + 1)
        updateTemplate(updatedTemplate)

        // 创建卡片
        val now = Clock.System.now()
        return Card(
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
    }
}
