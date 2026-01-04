package tech.zhifu.app.myhub.datastore.database.manage

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * 数据库管理器
 * 负责从 JSON 文件加载数据并写入数据库
 */
class DatabaseManager(
    private val database: MyHubDatabase
) {
    private val cardLoader = CardDataLoader(database)
    private val tagLoader = TagDataLoader(database)
    private val userLoader = UserDataLoader(database)
    private val templateLoader = TemplateDataLoader(database)

    /**
     * 从资源文件加载所有表的数据
     * @param resourcePath 资源文件路径，默认为 "database/init"
     * @param clearBeforeLoad 是否在加载前清空表数据，默认为 false
     * @param defaultUserId 默认用户ID，如果为 null，则使用第一个加载的用户ID
     */
    suspend fun loadAllData(
        resourcePath: String = "database/init",
        clearBeforeLoad: Boolean = false,
        defaultUserId: String? = null
    ) = withContext(Dispatchers.Default) {
        if (clearBeforeLoad) {
            clearAllData()
        }

        // 按依赖顺序加载：User -> Tag -> Card -> Template
        userLoader.loadFromResource("$resourcePath/user.json")

        // 获取默认用户ID（使用第一个用户或提供的 defaultUserId）
        val userId = defaultUserId ?: getFirstUserId() ?: "default-user"

        // 加载业务数据，使用获取到的 user_id
        tagLoader.loadFromResource("$resourcePath/tag.json", userId)
        cardLoader.loadFromResource("$resourcePath/card.json", userId)
        templateLoader.loadFromResource("$resourcePath/template.json", userId)
    }

    /**
     * 获取第一个用户的ID
     */
    private suspend fun getFirstUserId(): String? = withContext(Dispatchers.Default) {
        // 查询当前用户（第一个用户）
        val user = database.userQueries.selectCurrentUser().awaitAsOneOrNull()
        return@withContext user?.id
    }

    /**
     * 清空所有表的数据
     */
    suspend fun clearAllData() = withContext(Dispatchers.Default) {
        // 先获取第一个用户ID（用于清空业务数据）
        val userId = getFirstUserId()
        
        if (userId != null) {
            // 清空业务数据（需要 userId）
            cardLoader.clearData(userId)
            tagLoader.clearData(userId)
            templateLoader.clearData(userId)
        }
        
        // 清空用户数据（删除所有用户，这会触发外键级联删除）
        userLoader.clearData()
    }
}

