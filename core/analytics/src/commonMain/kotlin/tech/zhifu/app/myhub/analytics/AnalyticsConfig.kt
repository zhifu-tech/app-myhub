package tech.zhifu.app.myhub.analytics

/**
 * 统计配置
 */
data class AnalyticsConfig(
    /** 当前地区 */
    val region: Region,
    /** 是否启用统计 */
    val enabled: Boolean = true,
    /** 是否启用调试模式 */
    val debugMode: Boolean = false,
    /** 提供商配置列表 */
    val providers: List<ProviderConfig> = emptyList()
)

/**
 * 提供商配置
 */
data class ProviderConfig(
    /** 提供商类型 */
    val type: ProviderType,
    /** 是否启用 */
    val enabled: Boolean = true,
    /** 应用 Key（如友盟 AppKey、Firebase AppId） */
    val appKey: String? = null,
    /** 应用 Secret（如友盟 AppSecret） */
    val appSecret: String? = null,
    /** 其他自定义参数 */
    val customParams: Map<String, String> = emptyMap()
)

/**
 * 提供商类型
 */
enum class ProviderType {
    CONSOLE,         // 控制台输出（用于测试和 Desktop）
    FILE,             // 文件输出（用于 Desktop QA）
    FIREBASE,        // Firebase Analytics
    UMENG,           // 友盟+
}
