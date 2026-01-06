package tech.zhifu.app.myhub.analytics.di

import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.provider.AndroidAnalyticsRegistrar

/**
 * Android 平台 Analytics Provider 注册器实现
 * 默认注册 ConsoleProvider，后续可以注册 Android 特定的 Provider（如 Umeng、Firebase）
 */
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    return AndroidAnalyticsRegistrar()
}
