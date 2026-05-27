package tech.zhifu.app.myhub.logger

/**
 * Logger 配置
 */
data class LoggerConfig(
    val appName: String,
    // platform config
    val useAndroidLogger: Boolean = true,
    val enableDebugLogs: Boolean = false
)
