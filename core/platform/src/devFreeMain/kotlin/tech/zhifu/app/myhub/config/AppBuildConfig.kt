package tech.zhifu.app.myhub.config

/**
 * 开发环境 + 免费版配置
 */
object AppBuildConfig {
    val environment: BuildEnvironment = BuildEnvironment.DEVELOPMENT
    val versionType: VersionType = VersionType.FREE

    // FIXME: 2025/12/29  @zzf
    val apiBaseUrl: String = "http://192.168.0.123:8083" // adb reverse tcp:8083 tcp:8083

    //    val apiBaseUrl: String = "https://dev-api.myhub.app"
    val appName: String = "MyHub Dev (Free)"
    val applicationIdSuffix: String? = ".dev.free"
    val enableLogging: Boolean = true
    val enableDebugFeatures: Boolean = true
}
