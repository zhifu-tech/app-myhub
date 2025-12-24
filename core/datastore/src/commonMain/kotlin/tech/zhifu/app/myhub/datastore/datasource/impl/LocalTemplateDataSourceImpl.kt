package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import kotlin.time.Instant

/**
 * 本地模板数据源实现（使用SQLDelight）
 */
class LocalTemplateDataSourceImpl(
    private val database: MyHubDatabase
) : LocalTemplateDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getAllTemplates(): List<Template> {
        return database.templateQueries.selectAll().awaitAsList().map { it.toTemplate() }
    }

    override suspend fun getTemplateById(id: String): Template? {
        return database.templateQueries.selectById(id).awaitAsOneOrNull()?.toTemplate()
    }

    override suspend fun insertTemplate(template: Template) {
        database.templateQueries.insertTemplate(
            id = template.id,
            name = template.name,
            description = template.description,
            card_type = template.cardType.name,
            preview_image_url = template.previewImageUrl,
            default_content = template.defaultContent,
            default_tags = if (template.defaultTags.isNotEmpty()) {
                json.encodeToString(ListSerializer(String.serializer()), template.defaultTags)
            } else null,
            usage_count = template.usageCount.toLong(),
            is_system_template = if (template.isSystemTemplate) 1L else 0L,
            created_at = template.createdAt.toString(),
            updated_at = template.updatedAt.toString()
        )
    }

    override suspend fun updateTemplate(template: Template) {
        database.templateQueries.updateTemplate(
            name = template.name,
            description = template.description,
            card_type = template.cardType.name,
            preview_image_url = template.previewImageUrl,
            default_content = template.defaultContent,
            default_tags = if (template.defaultTags.isNotEmpty()) {
                json.encodeToString(ListSerializer(String.serializer()), template.defaultTags)
            } else null,
            usage_count = template.usageCount.toLong(),
            updated_at = template.updatedAt.toString(),
            id = template.id
        )
    }

    override suspend fun deleteTemplate(id: String) {
        database.templateQueries.deleteTemplate(id)
    }

    override fun observeTemplates(): Flow<List<Template>> {
        return database.templateQueries.selectAll()
            .asFlow()
            .mapLatest { query ->
                query.awaitAsList().map { it.toTemplate() }
            }
    }

    /**
     * 将数据库行转换为Template实体
     */
    private fun tech.zhifu.app.myhub.datastore.database.Template.toTemplate(): Template {
        val defaultTags = default_tags?.let {
            try {
                json.decodeFromString(ListSerializer(String.serializer()), it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return Template(
            id = id,
            name = name,
            description = description,
            cardType = CardType.valueOf(card_type),
            previewImageUrl = preview_image_url,
            defaultContent = default_content,
            defaultMetadata = null, // 如果需要可以扩展存储
            defaultTags = defaultTags,
            usageCount = usage_count.toInt(),
            isSystemTemplate = is_system_template == 1L,
            createdAt = Instant.parse(created_at),
            updatedAt = Instant.parse(updated_at)
        )
    }
}

