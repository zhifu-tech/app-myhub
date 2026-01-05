package tech.zhifu.app.myhub.analytics.di

import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar

/**
 * iOS 平台 Analytics Provider 注册器实现
 * 目前返回 null，后续可以注册 iOS 特定的 Provider（如 Umeng、Firebase）
 */
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    // TODO: 注册 iOS 特定的 Provider
    // return IosAnalyticsRegistrar()
    return null
}
