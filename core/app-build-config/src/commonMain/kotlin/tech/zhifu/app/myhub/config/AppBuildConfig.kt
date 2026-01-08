package tech.zhifu.app.myhub.config

object AppBuildConfig : AppBuildEnvConfig by AppBuildEnvConfigImpl(), AppBuildTierConfig by AppBuildTierConfigImpl(),
    AppBuildChannelConfig by AppBuildChannelConfigImpl() {

    const val APP_NAME: String = "MyHub"
}

interface AppBuildEnvConfig {
    val appEnv: String
    val enableLogging: Boolean
    val enableDebugFeatures: Boolean
}

interface AppBuildTierConfig {
    val appTier: String
}

interface AppBuildChannelConfig {
    val appChannel: String?
}
