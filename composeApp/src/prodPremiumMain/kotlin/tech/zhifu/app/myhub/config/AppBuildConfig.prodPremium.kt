package tech.zhifu.app.myhub.config

/**
 * 生产环境 + 付费版配置
 */
actual object AppBuildConfig {
    actual val environment: BuildEnvironment = BuildEnvironment.PRODUCTION
    actual val versionType: VersionType = VersionType.PREMIUM
    actual val apiBaseUrl: String = "https://api.myhub.app"
    actual val appName: String = "MyHub Premium"
    actual val applicationIdSuffix: String? = ".premium"
    actual val enableLogging: Boolean = false
    actual val enableDebugFeatures: Boolean = false
}

