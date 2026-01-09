package tech.zhifu.app.myhub.analytics.provider

import android.content.Context
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
        factory.register(ProviderType.UMENG) {
            UmengProvider(
                context = org.koin.core.context.GlobalContext.get().get<Context>(),
                name = "Umeng",
                supportedPlatforms = setOf(Platform.ANDROID),
                supportedRegions = setOf(Region.DOMESTIC),
            )
        }
    }
}


internal fun MutableList<ProviderConfig>.addChannelList() {
    add(
        ProviderConfig(
            type = ProviderType.UMENG,
            appKey = "695db2e99a7f376488244210"
        )
    )
}
