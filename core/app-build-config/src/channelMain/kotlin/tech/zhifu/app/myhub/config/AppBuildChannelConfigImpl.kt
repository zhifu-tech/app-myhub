package tech.zhifu.app.myhub.config

/**
 * 默认渠道配置实现
 * 覆盖 commonMain 中的默认渠道配置
 */
data class AppBuildChannelConfigImpl(
    override val appChannel: String? = "channel"
) : AppBuildChannelConfig
