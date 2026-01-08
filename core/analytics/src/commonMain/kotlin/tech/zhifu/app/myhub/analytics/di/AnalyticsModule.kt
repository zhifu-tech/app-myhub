package tech.zhifu.app.myhub.analytics.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.analytics.AnalyticsConsent
import tech.zhifu.app.myhub.analytics.AnalyticsManager
import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.DefaultAnalyticsConsent
import tech.zhifu.app.myhub.config.AppBuildConfig
import kotlin.coroutines.CoroutineContext

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
}

/**
 * 应用级 CoroutineScope
 * 用于管理统计服务的初始化，避免使用 GlobalScope
 */
class AppCoroutineScope : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    fun cancel() {
        job.cancel()
    }
}
