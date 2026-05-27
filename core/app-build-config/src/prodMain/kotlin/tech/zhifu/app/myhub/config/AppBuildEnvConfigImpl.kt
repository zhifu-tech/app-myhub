package tech.zhifu.app.myhub.config

data class AppBuildEnvConfigImpl(
    override val appEnv: String = "prod",
    override val enableLogging: Boolean = false,
    override val enableDebugFeatures: Boolean = false,
    override val enableServer: Boolean = true
) : AppBuildEnvConfig
