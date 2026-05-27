package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType

/**
 * JVM 平台（Desktop）Provider 注册器
 */
class JvmAnalyticsRegistrar : AnalyticsProviderRegistrar {

    override fun register(factory: AnalyticsProviderFactory) {

        // 注册 JVM 平台特定的 FileProvider
        factory.register(ProviderType.FILE) { config ->
            val outputDir = config.customParams["outputDir"] ?: "./analytics"
            val format = when (config.customParams["format"]?.uppercase()) {
                "CSV" -> FileProvider.FileFormat.CSV
                else -> FileProvider.FileFormat.JSON
            }
            FileProvider(outputDir = outputDir, format = format)
        }
    }
}
