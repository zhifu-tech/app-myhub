package tech.zhifu.app.myhub.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.analytics.AnalyticsConfig
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.config.AppBuildConfig

/**
 * JVM 平台（Desktop）特定的 Analytics 模块
 * JVM 平台主要用于 Desktop 应用，通常使用 ConsoleProvider 和 FileProvider
 */
internal actual fun analyticsPlatformModule(): Module = module {
    factory<AnalyticsConfig> {
        AnalyticsConfig(
            region = if (AppBuildConfig.isGooglePlay()) Region.OVERSEAS else Region.DOMESTIC,
            enabled = true,
            debugMode = AppBuildConfig.enableDebugFeatures,
            providers = buildList {
                // Console Provider（所有平台都支持，用于调试）
                add(ProviderConfig(type = ProviderType.CONSOLE))
                // Desktop 应用可以使用 FileProvider
                add(ProviderConfig(type = ProviderType.FILE))
            },
        )
    }
}
