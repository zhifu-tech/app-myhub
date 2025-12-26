package tech.zhifu.app.myhub.datastore.database.manage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import tech.zhifu.app.myhub.datastore.database.manage.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.model.Template

/**
 * 模板数据加载器
 */
class TemplateDataLoader(
    private val database: MyHubDatabase
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
        }
    }

    /**
     * 从资源文件加载数据
     * @param resourcePath 资源文件路径，相对于 composeResources 目录（例如："database/init/template.json"）
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadFromResource(resourcePath: String) = withContext(Dispatchers.Default) {
        val jsonString = Res.readBytes("files/$resourcePath").decodeToString()
        val templates = json.decodeFromString<List<Template>>(jsonString)
        insertTemplates(templates)
    }

    /**
     * 插入模板数据
     */
    private suspend fun insertTemplates(templates: List<Template>) = withContext(Dispatchers.Default) {
        database.transaction {
            templates.forEach { template ->
                // 将标签列表序列化为 JSON 字符串
                val defaultTagsJson = if (template.defaultTags.isNotEmpty()) {
                    json.encodeToString(
                        ListSerializer(String.serializer()),
                        template.defaultTags
                    )
                } else {
                    null
                }

                database.templateQueries.insertTemplate(
                    id = template.id,
                    name = template.name,
                    description = template.description,
                    card_type = template.cardType.name,
                    preview_image_url = template.previewImageUrl,
                    default_content = template.defaultContent,
                    default_tags = defaultTagsJson,
                    usage_count = template.usageCount.toLong(),
                    is_system_template = if (template.isSystemTemplate) 1L else 0L,
                    created_at = template.createdAt.toString(),
                    updated_at = template.updatedAt.toString()
                )
            }
        }
    }

    /**
     * 清空模板数据
     */
    suspend fun clearData() = withContext(Dispatchers.Default) {
        database.templateQueries.deleteAll()
    }
}


