package tech.zhifu.app.myhub.di

import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory

/**
 * Desktop (JVM) 平台特定模块
 */
actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }
}

