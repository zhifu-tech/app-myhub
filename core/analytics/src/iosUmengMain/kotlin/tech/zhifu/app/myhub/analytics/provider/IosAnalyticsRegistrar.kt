package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.Region

/**
 * iOS 平台 Provider 注册器
 */
class IosAnalyticsRegistrar : AnalyticsProviderRegistrar {

    override fun register(factory: AnalyticsProviderFactory) {
        // 注册 Umeng Provider（iOS 平台，国内渠道）
        factory.register(ProviderType.UMENG) { config ->
            UmengProvider(
                name = "Umeng",
                supportedPlatforms = setOf(Platform.IOS),
                supportedRegions = setOf(Region.DOMESTIC),
            )
        }
    }
}

internal fun MutableList<ProviderConfig>.addChannelList() {
    add(
        ProviderConfig(
            type = ProviderType.UMENG,
            appKey = "6960a2528560e34872205a67",
        )
    )
}
