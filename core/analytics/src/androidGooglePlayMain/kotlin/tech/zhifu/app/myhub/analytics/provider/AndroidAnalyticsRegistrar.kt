package tech.zhifu.app.myhub.analytics.provider

import dev.gitlive.firebase.FirebaseOptions
import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.Region

/**
 * Android 平台 Provider 注册器
 *  To enable debug logging run:
 *      adb shell setprop log.tag.FA VERBOSE
 * To enable faster debug mode event logging run:
 *      adb shell setprop debug.firebase.analytics.app tech.zhifu.app.myhub
 */
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
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
