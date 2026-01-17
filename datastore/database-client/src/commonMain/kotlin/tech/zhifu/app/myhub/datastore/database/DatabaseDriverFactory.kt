package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.module.Module

/**
 * 数据库驱动工厂
 * 各平台需要提供具体实现
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

expect fun databaseDriverFactoryModule(): Module
