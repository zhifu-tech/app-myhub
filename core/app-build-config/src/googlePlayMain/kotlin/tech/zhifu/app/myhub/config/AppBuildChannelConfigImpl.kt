package tech.zhifu.app.myhub.config

/**
 * Google Play 渠道配置实现
 * 覆盖 commonMain 中的默认渠道配置
 */
data class AppBuildChannelConfigImpl(
    override val appChannel: String? = "googlePlay"
) : AppBuildChannelConfig
