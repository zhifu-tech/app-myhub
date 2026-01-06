package tech.zhifu.app.myhub.config

object AppBuildConfig :
    AppBuildEnvConfig by AppBuildEnvConfigImpl(),
    AppBuildTierConfig by AppBuildTierConfigImpl(),
    AppBuildChannelConfig by AppBuildChannelConfigImpl() {

    const val APP_NAME: String = "MyHub"
    val applicationIdSuffix: String? = null // 不再使用组合后缀
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
