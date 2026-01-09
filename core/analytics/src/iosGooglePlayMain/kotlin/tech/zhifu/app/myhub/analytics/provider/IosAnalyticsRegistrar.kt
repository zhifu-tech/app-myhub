package tech.zhifu.app.myhub.analytics.provider

import dev.gitlive.firebase.FirebaseOptions
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

        // 注册 Android 平台特定的 FirebaseProvider
        factory.register(ProviderType.FIREBASE) { config ->
            FirebaseProvider(
                name = "Firebase",
                supportedPlatforms = setOf(Platform.ANDROID),
                supportedRegions = setOf(Region.OVERSEAS),
            )
        }
    }
}

internal fun ProviderConfig.toFirebaseOptions(): FirebaseOptions? = null

internal fun MutableList<ProviderConfig>.addChannelList() {
    add(
        ProviderConfig(
            type = ProviderType.FIREBASE,
        )
    )
}
