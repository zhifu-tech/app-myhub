package tech.zhifu.app.myhub.config

/**
 * 生产环境 + 免费版配置
 */
actual object AppBuildConfig {
    actual val environment: BuildEnvironment = BuildEnvironment.PRODUCTION
    actual val versionType: VersionType = VersionType.FREE
    actual val apiBaseUrl: String = "https://api.myhub.app"
    actual val appName: String = "MyHub (Free)"
    actual val applicationIdSuffix: String? = ".free"
    actual val enableLogging: Boolean = false
    actual val enableDebugFeatures: Boolean = false
}

