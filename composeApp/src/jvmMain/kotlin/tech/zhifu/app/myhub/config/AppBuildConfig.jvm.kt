package tech.zhifu.app.myhub.config

/**
 * JVM 平台默认配置（开发环境 + 免费版）
 * 
 * 注意：JVM 平台使用默认配置，可以通过环境变量或构建参数切换
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

