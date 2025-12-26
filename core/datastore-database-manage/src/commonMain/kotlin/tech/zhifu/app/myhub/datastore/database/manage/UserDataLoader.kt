package tech.zhifu.app.myhub.datastore.database.manage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import tech.zhifu.app.myhub.datastore.database.manage.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences

/**
 * 用户数据加载器
 */
class UserDataLoader(
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
     * @param resourcePath 资源文件路径，相对于 composeResources 目录（例如："database/init/user.json"）
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadFromResource(resourcePath: String) = withContext(Dispatchers.Default) {
        val jsonString = Res.readBytes("files/$resourcePath").decodeToString()
        val users = json.decodeFromString<List<User>>(jsonString)
        insertUsers(users)
    }

    /**
     * 插入用户数据
     */
    private suspend fun insertUsers(users: List<User>) = withContext(Dispatchers.Default) {
        database.transaction {
            users.forEach { user ->
                // 插入用户
                database.userQueries.insertUser(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    display_name = user.displayName,
                    avatar_url = user.avatarUrl,
                    created_at = user.createdAt.toString()
                )

                // 插入用户偏好设置（如果有）
                user.preferences?.let { prefs ->
                    database.userQueries.insertOrUpdatePreferences(
                        user_id = user.id,
                        theme = prefs.theme,
                        language = prefs.language,
                        default_card_type = prefs.defaultCardType?.name,
                        auto_sync = if (prefs.autoSync) 1L else 0L,
                        sync_interval = prefs.syncInterval
                    )
                }
            }
        }
    }

    /**
     * 清空用户数据
     */
    suspend fun clearData() = withContext(Dispatchers.Default) {
        database.userQueries.deleteAll()
    }
}
