package tech.zhifu.app.myhub.datastore.database.manage.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.manage.DatabaseManager

/**
 * 数据库管理器依赖注入模块
 * 
 * 提供 DatabaseManager 实例，用于从 JSON 文件加载数据到数据库
 */
val databaseManagerModule = module {
    // 提供 DatabaseManager 单例实例
    single<DatabaseManager> {
        DatabaseManager(
            database = get<MyHubDatabase>()
        )
    }
}

