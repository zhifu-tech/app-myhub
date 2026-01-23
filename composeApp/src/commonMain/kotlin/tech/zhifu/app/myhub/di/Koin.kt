package tech.zhifu.app.myhub.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import tech.zhifu.app.myhub.analytics.AnalyticsManager
import tech.zhifu.app.myhub.analytics.di.analyticsModule
import tech.zhifu.app.myhub.component.card.di.cardModule
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.bootstrap.di.bootstrapModule
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.feature.card.di.cardDetailModule
import tech.zhifu.app.myhub.feature.dashboard.di.dashboardModule
import tech.zhifu.app.myhub.feature.profile.di.profileModule
import tech.zhifu.app.myhub.feature.settings.di.settingsModule
import tech.zhifu.app.myhub.logger.LoggerConfig
import tech.zhifu.app.myhub.logger.di.loggerModule
import kotlin.coroutines.CoroutineContext

fun initKoin(platformSpecificConfig: (KoinApplication.() -> Unit)? = null) {
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

    val appScope = AppCoroutineScope()

    val koinApplication = startKoin {
        // 应用平台特定配置（如果提供）
        platformSpecificConfig?.invoke(this)

        modules(
            loggerModule {
                LoggerConfig(
                    appName = "Myhub",
                    useAndroidLogger = true,
                )
            }, platformModule(),
            // Data module dependencies
            repositoryModule,
            bootstrapModule,
            settingsModule(),
            dashboardModule(),
            profileModule(),
            cardDetailModule(),
            // Component modules
            cardModule,
            // Analytics module
            analyticsModule(),
            // App module
            module {
                // 提供 AppCoroutineScope（用于 analytics 初始化）
                single<AppCoroutineScope> { appScope }
                // 提供 ViewModel 使用的 CoroutineScope
                // 使用 Dispatchers.Default 作为默认调度器
                factory<CoroutineScope> {
                    CoroutineScope(Dispatchers.Default)
                }
            })
    }

    appScope.launch {
        val userRepository = koinApplication.koin.get<UserRepository>()
        if (userRepository.hasUser().not()) {
            koinApplication.koin.get<Bootstrap>().initialize("default")
        }
    }

    // 初始化统计服务（延迟初始化，避免阻塞应用启动）
    appScope.launch {
        try {
            // 等待 Koin 初始化完成后再获取 AnalyticsManager
            delay(100)
            koinApplication.koin.get<AnalyticsManager>().initialize()
        } catch (e: Exception) {
            // 统计初始化失败不应影响应用启动
            println("Failed to initialize analytics: ${e.message}")
        }
    }
}
