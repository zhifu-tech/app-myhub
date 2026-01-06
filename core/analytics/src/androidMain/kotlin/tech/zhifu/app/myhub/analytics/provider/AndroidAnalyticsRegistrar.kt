package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar

/**
 * Android 平台 Provider 注册器
 */
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
    private val commonRegistrar = CommonAnalyticsRegistrar()

    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider）
        commonRegistrar.register(factory)

        // TODO: 注册 Android 特定的 Provider（如 Umeng、Firebase）
        // factory.register(ProviderType.UMENG) { config ->
        //     UmengProvider(config)
        // }
        // factory.register(ProviderType.FIREBASE) { config ->
        //     FirebaseProvider(config)
        // }
    }
}
