package tech.zhifu.app.myhub.config

data class AppBuildTierConfigImpl(
    override val appTier: String = "free",
) : AppBuildTierConfig
