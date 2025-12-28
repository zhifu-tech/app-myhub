package tech.zhifu.app.myhub.config

/**
 * 构建环境枚举
 */
enum class BuildEnvironment {
    DEVELOPMENT,  // 开发环境
    PRODUCTION    // 生产环境
}

/**
 * 版本类型枚举
 */
enum class VersionType {
    FREE,    // 免费版
    PREMIUM  // 付费版
}

/**
 * 获取环境描述
 */
fun getEnvironmentDescription(): String {
    val env = when (AppBuildConfig.environment) {
        BuildEnvironment.DEVELOPMENT -> "开发环境"
        BuildEnvironment.PRODUCTION -> "生产环境"
    }
    val version = when (AppBuildConfig.versionType) {
        VersionType.FREE -> "免费版"
        VersionType.PREMIUM -> "付费版"
    }
    return "$env - $version"
}
