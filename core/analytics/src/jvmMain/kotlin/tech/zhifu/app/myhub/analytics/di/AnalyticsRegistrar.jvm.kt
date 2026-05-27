package tech.zhifu.app.myhub.analytics.di

import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.provider.JvmAnalyticsRegistrar

/**
 * JVM 平台（Desktop）Analytics Provider 注册器实现
 */
internal actual fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar? {
    return JvmAnalyticsRegistrar()
}
