package tech.zhifu.app.myhub.analytics

/**
 * 统一统计服务接口
 * 业务代码通过此接口进行统计上报
 */
interface AnalyticsService {
    /**
     * 记录事件
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * 批量记录事件（用于高频事件场景）
     * 某些 Provider 可能支持批量上报以提高性能
     */
    fun logEvents(events: List<AnalyticsEvent>) {
        events.forEach { logEvent(it) }
    }

    /**
     * 设置用户属性
     */
    fun setUserProperty(key: String, value: AnalyticsValue?)

    /**
     * 设置用户 ID
     */
    fun setUserId(userId: String?)

    /**
     * 设置当前屏幕
     */
    fun setScreen(screenName: String, screenClass: String? = null)

    /**
     * 重置用户数据（登出时调用）
     */
    fun reset()
}
