package tech.zhifu.app.myhub.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory

/**
 * Android平台特定模块
 */
actual fun platformModule(): Module = module {
    single<Context> { get<android.app.Application>().applicationContext }
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(get())
    }
}

