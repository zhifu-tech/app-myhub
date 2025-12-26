package tech.zhifu.app.myhub.config

/**
 * 构建配置使用示例
 * 
 * 展示如何在代码中使用 AppBuildConfig
 */
object BuildConfigUsage {
    /**
     * 获取当前环境的 API URL
     */
    fun getApiUrl(): String {
        return AppBuildConfig.apiBaseUrl
    }
    
    /**
     * 获取应用显示名称
     */
    fun getAppDisplayName(): String {
        return AppBuildConfig.appName
    }
    
    /**
     * 检查是否为开发环境
     */
    fun isDevelopment(): Boolean {
        return AppBuildConfig.environment == BuildEnvironment.DEVELOPMENT
    }
    
    /**
     * 检查是否为付费版
     */
    fun isPremium(): Boolean {
        return AppBuildConfig.versionType == VersionType.PREMIUM
    }
    
    /**
     * 是否应该显示调试功能
     */
    fun shouldShowDebugFeatures(): Boolean {
        return AppBuildConfig.enableDebugFeatures
    }
    
    /**
     * 是否应该记录日志
     */
    fun shouldLog(): Boolean {
        return AppBuildConfig.enableLogging
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
}

