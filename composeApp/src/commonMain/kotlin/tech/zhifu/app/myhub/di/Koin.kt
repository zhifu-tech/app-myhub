package tech.zhifu.app.myhub.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.analytics.di.analyticsModule
import tech.zhifu.app.myhub.component.media.di.mediaModule
import tech.zhifu.app.myhub.datastore.bootstrap.di.bootstrapModule
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.feature.auth.di.authModule
import tech.zhifu.app.myhub.feature.capture.di.captureModule
import tech.zhifu.app.myhub.feature.card.di.cardDetailModule
import tech.zhifu.app.myhub.feature.dashboard.di.dashboardModule
import tech.zhifu.app.myhub.feature.profile.di.profileModule
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
            LoggerConfig(appName = "Myhub")
        },
        platformModule(),
        repositoryModule,
        bootstrapModule,
        authModule(),
        settingsModule(),
        dashboardModule(),
        profileModule(),
        mediaModule(),
        captureModule(),
        cardDetailModule(),
        // Analytics module
        analyticsModule(),
        startupModule()
    )
}
