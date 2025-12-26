package tech.zhifu.app.myhub.datastore.database.manage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import tech.zhifu.app.myhub.datastore.database.manage.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.model.Tag

/**
 * 标签数据加载器
 */
class TagDataLoader(
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
     * @param resourcePath 资源文件路径，相对于 composeResources 目录（例如："database/init/tag.json"）
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadFromResource(resourcePath: String) = withContext(Dispatchers.Default) {
        val jsonString = Res.readBytes("files/$resourcePath").decodeToString()
        val tags = json.decodeFromString<List<Tag>>(jsonString)
        insertTags(tags)
    }

    /**
     * 插入标签数据
     */
    private suspend fun insertTags(tags: List<Tag>) = withContext(Dispatchers.Default) {
        database.transaction {
            tags.forEach { tag ->
                database.tagQueries.insertTag(
                    id = tag.id,
                    name = tag.name,
                    color = tag.color,
                    description = tag.description,
                    card_count = tag.cardCount.toLong(),
                    created_at = tag.createdAt.toString()
                )
            }
        }
    }

    /**
     * 清空标签数据
     */
    suspend fun clearData() = withContext(Dispatchers.Default) {
        database.tagQueries.deleteAll()
    }
}


