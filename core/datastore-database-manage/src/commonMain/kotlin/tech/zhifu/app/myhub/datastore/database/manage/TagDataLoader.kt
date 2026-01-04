package tech.zhifu.app.myhub.datastore.database.manage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.manage.resources.Res
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
     * @param userId 用户ID，用于关联标签数据
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadFromResource(resourcePath: String, userId: String) = withContext(Dispatchers.Default) {
        val jsonString = Res.readBytes("files/$resourcePath").decodeToString()
        val tags = json.decodeFromString<List<Tag>>(jsonString)
        insertTags(tags, userId)
    }

    /**
     * 插入标签数据
     * @param tags 标签列表
     * @param userId 用户ID，用于关联标签数据（如果 JSON 中没有指定 userId）
     */
    private suspend fun insertTags(tags: List<Tag>, userId: String) = withContext(Dispatchers.Default) {
        database.transaction {
            tags.forEach { tag ->
                // 优先使用 JSON 中的 userId，如果没有则使用传入的参数
                val tagUserId = tag.userId ?: userId

                database.tagQueries.insertTag(
                    id = tag.id,
                    name = tag.name,
                    color = tag.color,
                    description = tag.description,
                    card_count = tag.cardCount.toLong(),
                    created_at = tag.createdAt.toString(),
                    user_id = tagUserId
                )
            }
        }
    }

    /**
     * 清空标签数据
     */
    suspend fun clearData(userId: String? = null) = withContext(Dispatchers.Default) {
        database.tagQueries.deleteAll(userId ?: "")
    }
}


