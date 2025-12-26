package tech.zhifu.app.myhub.datastore.database.manage

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
     */
    suspend fun loadAllData(
        resourcePath: String = "database/init",
        clearBeforeLoad: Boolean = false
    ) = withContext(Dispatchers.Default) {
        if (clearBeforeLoad) {
            clearAllData()
        }

        // 按依赖顺序加载：User -> Tag -> Card -> Template
        userLoader.loadFromResource("$resourcePath/user.json")
        tagLoader.loadFromResource("$resourcePath/tag.json")
        cardLoader.loadFromResource("$resourcePath/card.json")
        templateLoader.loadFromResource("$resourcePath/template.json")
    }

    /**
     * 清空所有表的数据
     */
    suspend fun clearAllData() = withContext(Dispatchers.Default) {
        cardLoader.clearData()
        tagLoader.clearData()
        userLoader.clearData()
        templateLoader.clearData()
    }
}

