package tech.zhifu.app.myhub.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.analytics.AnalyticsConfig
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.config.AppBuildConfig

/**
 * iOS 平台特定的 Analytics 模块
 * iOS 平台会自动从 GoogleService-Info.plist 读取 Firebase 配置，无需额外配置
 */
internal actual fun analyticsPlatformModule(): Module = module {
    factory<AnalyticsConfig> {
        val isGooglePlay = AppBuildConfig.appChannel == "googlePlay"
        AnalyticsConfig(
            region = if (isGooglePlay) Region.OVERSEAS else Region.DOMESTIC,
            enabled = true,
            debugMode = AppBuildConfig.enableDebugFeatures,
            providers = buildList {
                // Console Provider（所有平台都支持，用于调试）
                add(ProviderConfig(type = ProviderType.CONSOLE))
                if (isGooglePlay) {
                    add(
                        ProviderConfig(
                            type = ProviderType.FIREBASE,
                            // iOS 平台会自动从 GoogleService-Info.plist 读取配置
                            // 不需要 customParams
                        )
                    )
                } else {
                    add(ProviderConfig(type = ProviderType.UMENG))
                }
            },
        )
    }
}
