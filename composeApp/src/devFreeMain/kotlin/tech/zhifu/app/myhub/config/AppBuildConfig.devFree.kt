package tech.zhifu.app.myhub.config

/**
 * 开发环境 + 免费版配置
 */
actual object AppBuildConfig {
    actual val environment: BuildEnvironment = BuildEnvironment.DEVELOPMENT
    actual val versionType: VersionType = VersionType.FREE
    actual val apiBaseUrl: String = "https://dev-api.myhub.app"
    actual val appName: String = "MyHub Dev (Free)"
    actual val applicationIdSuffix: String? = ".dev.free"
    actual val enableLogging: Boolean = true
    actual val enableDebugFeatures: Boolean = true
}

