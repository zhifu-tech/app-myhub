package tech.zhifu.app.myhub.analytics.di

import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar

/**
 * WASM 平台 Analytics Provider 注册器实现
 * Web 平台可以使用 ConsoleProvider 或 Google Analytics
 */
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    // TODO: 注册 Web 特定的 Provider（如 Google Analytics）
    return null
}
