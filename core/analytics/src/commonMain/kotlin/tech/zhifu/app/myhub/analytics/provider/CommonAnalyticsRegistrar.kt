package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType

/**
 * 通用 Analytics Provider 注册器
 * 注册所有平台都支持的 Provider（如 ConsoleProvider）
 */
class CommonAnalyticsRegistrar : AnalyticsProviderRegistrar {
    override fun register(factory: AnalyticsProviderFactory) {
        // 注册 ConsoleProvider（所有平台都支持）
        factory.register(ProviderType.CONSOLE) { config ->
            ConsoleProvider()
        }
    }
}
