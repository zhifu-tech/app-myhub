package tech.zhifu.app.myhub.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.bind
import tech.zhifu.app.myhub.analytics.AnalyticsConsent
import tech.zhifu.app.myhub.analytics.AnalyticsManager
import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.DefaultAnalyticsConsent
import tech.zhifu.app.myhub.analytics.startup.AnalyticsStartupTask
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.startup.StartupTask

internal fun AppBuildConfig.isGooglePlay() = appChannel == "googlePlay"

internal expect fun analyticsPlatformModule(): Module

/**
 * Analytics Koin 模块
 */
fun analyticsModule(): Module = module {
    // 包含平台特定的模块
    includes(analyticsPlatformModule())

    factory<AnalyticsConsent> {
        DefaultAnalyticsConsent(
            analyticsAllowed = true, // 可以从用户设置获取
            personalizationAllowed = false
        )
    }
    factory<AnalyticsProviderFactory> {
        val factory = AnalyticsProviderFactory()
        // 自动注册平台特定的 Provider
        getAnalyticsRegistrar()?.register(factory)
        factory
    }
    single<AnalyticsManager> {
        AnalyticsManager(
            config = get(),
            consent = get(),
            providerFactory = get(),
        )
    }
    single<AnalyticsService> { get<AnalyticsManager>() }

    factory {
        AnalyticsStartupTask(get())
    } bind StartupTask::class
}
