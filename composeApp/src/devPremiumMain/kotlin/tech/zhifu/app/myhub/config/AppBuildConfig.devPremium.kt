package tech.zhifu.app.myhub.config

/**
 * 开发环境 + 付费版配置
 */
actual object AppBuildConfig {
    actual val environment: BuildEnvironment = BuildEnvironment.DEVELOPMENT
    actual val versionType: VersionType = VersionType.PREMIUM
    actual val apiBaseUrl: String = "https://dev-api.myhub.app"
    actual val appName: String = "MyHub Dev (Premium)"
    actual val applicationIdSuffix: String? = ".dev.premium"
    actual val enableLogging: Boolean = true
    actual val enableDebugFeatures: Boolean = true
}

