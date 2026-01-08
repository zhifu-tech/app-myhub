package tech.zhifu.app.myhub.analytics

import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn

/**
 * 统计管理器
 * 管理多个统计服务提供商，提供统一的统计接口
 */
class AnalyticsManager(
    private val config: AnalyticsConfig,
    private val consent: AnalyticsConsent,
    private val logger: Logger = logger("AnalyticsManager"),
    private val providerFactory: AnalyticsProviderFactory
) : AnalyticsService {

    private val providers = mutableListOf<AnalyticsProvider>()

    /**
     * 初始化所有启用的提供商
     */
    suspend fun initialize() {
        if (!config.enabled) {
            logger.info { "Analytics is disabled" }
            return
        }

        // 检查隐私合规
        if (!consent.isAnalyticsAllowed()) {
            logger.info { "Analytics is not allowed by user consent" }
            return
        }

        // Debug 模式下自动注入 ConsoleProvider（如果未配置）
        if (config.debugMode && config.providers.none { it.type == ProviderType.CONSOLE }) {
            try {
                val consoleProvider = providerFactory.create(
                    ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
                )
                consoleProvider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
                providers.add(consoleProvider)
                logger.info { "Debug mode: ConsoleProvider auto-injected" }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to auto-inject ConsoleProvider in debug mode" }
            }
        }

        config.providers.filter { it.enabled }.forEach { providerConfig ->
            try {
                val provider = providerFactory.create(providerConfig)
                if (provider.supportedRegions.contains(config.region)) {
                    provider.initialize(providerConfig)
                    providers.add(provider)
                    logger.info { "Analytics provider initialized: ${provider.name}" }
                } else {
                    logger.warn { "Provider ${provider.name} does not support region ${config.region}" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to initialize provider: ${providerConfig.type}" }
            }
        }
    }

    override fun logEvent(event: AnalyticsEvent) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.logEvent(event)
            } catch (e: Exception) {
                logger.error(e) { "Failed to log event to ${provider.name}" }
            }
        }
    }

    override fun logEvents(events: List<AnalyticsEvent>) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                // 如果 Provider 支持批量上报，可以优化
                events.forEach { provider.logEvent(it) }
            } catch (e: Exception) {
                logger.error(e) { "Failed to log events to ${provider.name}" }
            }
        }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.setUserProperty(key, value)
            } catch (e: Exception) {
                logger.error(e) { "Failed to set user property to ${provider.name}" }
            }
        }
    }

    override fun setUserId(userId: String?) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.setUserId(userId)
            } catch (e: Exception) {
                logger.error(e) { "Failed to set user ID to ${provider.name}" }
            }
        }
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.setScreen(screenName, screenClass)
            } catch (e: Exception) {
                logger.error(e) { "Failed to set screen to ${provider.name}" }
            }
        }
    }

    override fun reset() {
        if (!config.enabled) return

        providers.forEach { provider ->
            try {
                provider.reset()
                // 清理资源
                if (provider is BaseAnalyticsProvider) {
                    provider.cleanup()
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to reset ${provider.name}" }
            }
        }
        providers.clear()
    }
}
