package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar

/**
 * WasmJs 平台 Provider 注册器
 */
class WasmJsAnalyticsRegistrar : AnalyticsProviderRegistrar {

    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider）
        CommonAnalyticsRegistrar().register(factory)
    }
}
