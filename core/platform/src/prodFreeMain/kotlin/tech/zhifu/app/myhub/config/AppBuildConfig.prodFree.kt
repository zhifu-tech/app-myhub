package tech.zhifu.app.myhub.config

/**
 * 生产环境 + 免费版配置
 */
object AppBuildConfig {
    val environment: BuildEnvironment = BuildEnvironment.PRODUCTION
    val versionType: VersionType = VersionType.FREE
    val apiBaseUrl: String = "https://api.myhub.app"
    val appName: String = "MyHub (Free)"
    val applicationIdSuffix: String? = ".free"
    val enableLogging: Boolean = false
    val enableDebugFeatures: Boolean = false
}

