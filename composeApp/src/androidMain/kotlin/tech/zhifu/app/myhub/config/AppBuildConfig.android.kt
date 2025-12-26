package tech.zhifu.app.myhub.config

/**
 * Android 平台默认配置（开发环境 + 免费版）
 *
 * 注意：Android 平台会通过 productFlavors 选择对应的变体实现
 * 如果没有匹配的变体，则使用此默认配置
 */
actual object AppBuildConfig {
    actual val environment: BuildEnvironment = BuildEnvironment.DEVELOPMENT
    actual val versionType: VersionType = VersionType.FREE
    actual val apiBaseUrl: String = "http://127.0.0.1:8083" // Android 模拟器使用 10.0.2.2 访问主机 localhost
    actual val appName: String = "MyHub Dev (Free)"
    actual val applicationIdSuffix: String? = ".dev.free"
    actual val enableLogging: Boolean = true
    actual val enableDebugFeatures: Boolean = true
}

