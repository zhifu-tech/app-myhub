package tech.zhifu.app.myhub.config

/**
 * 开发环境 + 付费版配置
 */
object AppBuildConfig {
    val environment: BuildEnvironment = BuildEnvironment.DEVELOPMENT
    val versionType: VersionType = VersionType.PREMIUM
    val apiBaseUrl: String = "https://dev-api.myhub.app"
    val appName: String = "MyHub Dev (Premium)"
    val applicationIdSuffix: String? = ".dev.premium"
    val enableLogging: Boolean = true
    val enableDebugFeatures: Boolean = true
}
