package tech.zhifu.app.myhub.config

/**
 * 生产环境 + 免费版配置
 * 
 * 注意：变体源集通过 srcDir 注入到 commonMain，因此这里直接实现 expect 声明
 * 而不是使用 actual 关键字（actual 只能用于平台特定的源集）
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

