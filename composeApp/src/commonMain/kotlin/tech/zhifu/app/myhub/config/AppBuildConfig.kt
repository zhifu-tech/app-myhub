package tech.zhifu.app.myhub.config

/**
 * 应用构建配置
 * 
 * 使用 expect/actual 模式支持不同变体的配置
 */
expect object AppBuildConfig {
    /**
     * 环境类型
     */
    val environment: BuildEnvironment
    
    /**
     * 版本类型
     */
    val versionType: VersionType
    
    /**
     * API 基础 URL
     */
    val apiBaseUrl: String
    
    /**
     * 应用名称
     */
    val appName: String
    
    /**
     * 应用 ID 后缀（用于区分变体）
     */
    val applicationIdSuffix: String?
    
    /**
     * 是否启用日志
     */
    val enableLogging: Boolean
    
    /**
     * 是否启用调试功能
     */
    val enableDebugFeatures: Boolean
}

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

