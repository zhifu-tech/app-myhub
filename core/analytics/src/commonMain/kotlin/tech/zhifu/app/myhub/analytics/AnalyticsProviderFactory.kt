package tech.zhifu.app.myhub.analytics

import tech.zhifu.app.myhub.analytics.provider.ConsoleProvider
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger

/**
 * Provider 创建器类型
 */
private typealias ProviderCreator = (ProviderConfig) -> AnalyticsProvider

/**
 * 统计服务提供商工厂
 * 使用 Registry 模式，支持动态注册 Provider
 */
class AnalyticsProviderFactory(
    private val logger: Logger = logger("AnalyticsProviderFactory")
) {
    /**
     * Provider 注册表
     */
    private val creators = mutableMapOf<ProviderType, ProviderCreator>()

    init {
        // 注册默认 Provider
        registerDefaultProviders()
    }

    /**
     * 注册 Provider 创建器
     */
    fun register(type: ProviderType, creator: ProviderCreator) {
        creators[type] = creator
        logger.debug { "Registered analytics provider: $type" }
    }

    /**
     * 创建 Provider 实例
     */
    fun create(config: ProviderConfig): AnalyticsProvider {
        val creator = creators[config.type]
            ?: throw IllegalArgumentException("Unknown provider type: ${config.type}")

        return try {
            creator(config)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create provider: ${config.type}" }
            throw e
        }
    }

    /**
     * 注册默认 Provider
     */
    private fun registerDefaultProviders() {
        // 注册 ConsoleProvider（所有平台都支持）
        register(ProviderType.CONSOLE) { config ->
            ConsoleProvider()
        }
    }
}

/**
 * Provider 注册器接口
 * 各平台模块实现此接口，在应用启动时注册平台特定的 Provider
 */
interface AnalyticsProviderRegistrar {
    /**
     * 注册该平台支持的 Provider
     */
    fun register(factory: AnalyticsProviderFactory)
}
