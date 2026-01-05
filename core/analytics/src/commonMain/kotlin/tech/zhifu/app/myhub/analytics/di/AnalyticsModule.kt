package tech.zhifu.app.myhub.analytics.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.analytics.AnalyticsConfig
import tech.zhifu.app.myhub.analytics.AnalyticsConsent
import tech.zhifu.app.myhub.analytics.AnalyticsManager
import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.DefaultAnalyticsConsent

/**
 * Analytics Koin 模块
 */
fun analyticsModule(
    config: () -> AnalyticsConfig,
    consent: AnalyticsConsent = DefaultAnalyticsConsent()
): Module = module {
    single<AnalyticsConfig> { config() }

    single<AnalyticsConsent> { consent }

    single<AnalyticsProviderFactory> {
        val factory = AnalyticsProviderFactory()
        // 自动注册平台特定的 Provider
        getAnalyticsRegistrar()?.register(factory)
        factory
    }

    single<AnalyticsManager> {
        AnalyticsManager(
            config = get(),
            consent = get(),
            providerFactory = get()
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
