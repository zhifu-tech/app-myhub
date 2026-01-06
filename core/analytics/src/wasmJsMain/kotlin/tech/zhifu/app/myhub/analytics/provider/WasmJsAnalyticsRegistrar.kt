package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar

/**
 * WASM 平台 Provider 注册器
 */
class WasmJsAnalyticsRegistrar : AnalyticsProviderRegistrar {
    private val commonRegistrar = CommonAnalyticsRegistrar()

    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider）
        commonRegistrar.register(factory)

        // TODO: 注册 Web 特定的 Provider（如 Google Analytics）
        // factory.register(ProviderType.GOOGLE_ANALYTICS) { config ->
        //     GoogleAnalyticsProvider(config)
        // }
    }
}
