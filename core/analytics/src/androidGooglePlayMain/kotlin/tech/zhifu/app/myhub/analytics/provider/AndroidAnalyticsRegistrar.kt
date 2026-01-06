package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.ProviderType

/**
 * Android 平台 Provider 注册器
 */
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider）
        CommonAnalyticsRegistrar().register(factory)

        factory.register(ProviderType.FIREBASE) { config ->
            FirebaseProvider()
        }
    }
}
