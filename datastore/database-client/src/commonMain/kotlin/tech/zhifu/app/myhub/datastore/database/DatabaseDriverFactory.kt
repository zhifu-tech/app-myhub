package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.module.Module

fun interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

expect fun databaseDriverFactoryModule(): Module
