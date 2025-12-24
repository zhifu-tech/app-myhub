package tech.zhifu.app.myhub.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.datastore.di.dataModule
import tech.zhifu.app.myhub.datastore.di.databaseModule
import tech.zhifu.app.myhub.datastore.di.networkModule

fun initKoin(platformSpecificConfig: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        // 应用平台特定配置（如果提供）
        platformSpecificConfig?.invoke(this)

        modules(
            platformModule(),
            // Data module dependencies (from core:data)
            networkModule,
            databaseModule,
            dataModule
        )
    }
}
