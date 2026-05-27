package tech.zhifu.app.myhub.config

data class AppBuildEnvConfigImpl(
    override val appEnv: String = "dev",
    override val enableLogging: Boolean = true,
    override val enableDebugFeatures: Boolean = true,
    override val enableServer: Boolean = false
) : AppBuildEnvConfig
