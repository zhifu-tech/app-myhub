package tech.zhifu.app.myhub.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.analytics.AnalyticsConfig
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.analytics.provider.addChannelList
import tech.zhifu.app.myhub.config.AppBuildConfig

internal actual fun analyticsPlatformModule(): Module = module {
    factory<AnalyticsConfig> {
        AnalyticsConfig(
            region = if (AppBuildConfig.isGooglePlay()) Region.OVERSEAS else Region.DOMESTIC,
            enabled = true,
            debugMode = AppBuildConfig.enableDebugFeatures,
            providers = buildList {
                // Console Provider（所有平台都支持，用于调试）
                add(ProviderConfig(type = ProviderType.CONSOLE))
                addChannelList()
            },
        )
    }
}
