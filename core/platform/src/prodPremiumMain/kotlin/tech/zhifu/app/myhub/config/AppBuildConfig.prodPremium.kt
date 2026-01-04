package tech.zhifu.app.myhub.config

/**
 * 生产环境 + 付费版配置
 */
object AppBuildConfig {
    val environment: BuildEnvironment = BuildEnvironment.PRODUCTION
    val versionType: VersionType = VersionType.PREMIUM
    val apiBaseUrl: String = "https://api.myhub.app"
    val appName: String = "MyHub Premium"
    val applicationIdSuffix: String? = ".premium"
    val enableLogging: Boolean = false
    val enableDebugFeatures: Boolean = false
}
