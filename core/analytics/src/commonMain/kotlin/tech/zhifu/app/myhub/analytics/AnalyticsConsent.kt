package tech.zhifu.app.myhub.analytics

/**
 * 统计服务隐私合规接口
 * 用于处理 GDPR、CCPA 等隐私法规要求
 */
interface AnalyticsConsent {
    /**
     * 是否允许统计
     */
    fun isAnalyticsAllowed(): Boolean

    /**
     * 是否允许个性化统计
     */
    fun isPersonalizationAllowed(): Boolean

    /**
     * 更新用户同意状态
     */
    fun updateConsent(
        analyticsAllowed: Boolean,
        personalizationAllowed: Boolean = false
    )
}

/**
 * 默认实现：始终允许（用于测试或不需要合规的场景）
 */
class DefaultAnalyticsConsent(
    private var analyticsAllowed: Boolean = true,
    private var personalizationAllowed: Boolean = false
) : AnalyticsConsent {
    override fun isAnalyticsAllowed() = analyticsAllowed
    override fun isPersonalizationAllowed() = personalizationAllowed

    override fun updateConsent(
        analyticsAllowed: Boolean,
        personalizationAllowed: Boolean
    ) {
        this.analyticsAllowed = analyticsAllowed
        this.personalizationAllowed = personalizationAllowed
    }
}
