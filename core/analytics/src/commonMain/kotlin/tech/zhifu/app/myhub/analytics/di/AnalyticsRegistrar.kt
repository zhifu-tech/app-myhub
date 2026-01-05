package tech.zhifu.app.myhub.analytics.di

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar

/**
 * 获取平台特定的 Analytics Provider 注册器
 * 使用 expect/actual 机制实现平台特定注册
 */
internal expect fun getAnalyticsRegistrar(): AnalyticsProviderRegistrar?
