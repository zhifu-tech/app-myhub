package tech.zhifu.app.myhub.config

object AppBuildConfig :
    AppBuildEnvConfig by AppBuildEnvConfigImpl(),
    AppBuildTierConfig by AppBuildTierConfigImpl(),
    AppBuildChannelConfig by AppBuildChannelConfigImpl()

interface AppBuildEnvConfig {
    val appEnv: String
    val enableLogging: Boolean
    val enableDebugFeatures: Boolean
    val enableServer: Boolean
}

interface AppBuildTierConfig {
    val appTier: String
}

interface AppBuildChannelConfig {
    val appChannel: String?
}
