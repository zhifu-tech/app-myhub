package tech.zhifu.app.myhub.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.AppViewModel
import tech.zhifu.app.myhub.analytics.di.analyticsModule
import tech.zhifu.app.myhub.component.media.di.mediaModule
import tech.zhifu.app.myhub.datastore.bootstrap.di.bootstrapModule
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.feature.ai.di.aiModule
import tech.zhifu.app.myhub.feature.dashboard.di.dashboardModule
import tech.zhifu.app.myhub.feature.settings.di.settingsModule
import tech.zhifu.app.myhub.logger.LoggerConfig
import tech.zhifu.app.myhub.logger.di.loggerModule
import tech.zhifu.app.myhub.startup.di.startupModule

fun initKoin(
    platformSpecificConfig: (KoinApplication.() -> Unit)? = null
): KoinApplication = startKoin {
    // 应用平台特定配置（如果提供）
    platformSpecificConfig?.invoke(this)

    modules(
        loggerModule {
            LoggerConfig(
                appName = "Myhub",
                enableDebugLogs = AppBuildConfig.enableDebugFeatures
            )
        },

        module {
            viewModelOf(::AppViewModel)
        },

        platformModule(),
        repositoryModule(),
        startupModule(),
        analyticsModule(),
        mediaModule(),
        bootstrapModule(),
        aiModule(),
        dashboardModule(),
        settingsModule(),
    )
}
