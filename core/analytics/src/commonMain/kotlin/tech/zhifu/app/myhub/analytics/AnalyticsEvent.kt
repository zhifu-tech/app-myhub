package tech.zhifu.app.myhub.analytics

/**
 * 统计事件模型
 */
data class AnalyticsEvent(
    /** 事件名称（建议使用 AnalyticsEvents 常量） */
    val name: String,
    /** 事件参数（使用类型安全的 AnalyticsValue） */
    val parameters: Map<String, AnalyticsValue> = emptyMap(),
    /** 事件值（可选） */
    val value: Double? = null,
    /** 货币单位（可选，用于电商场景） */
    val currency: String? = null
) {
    companion object {
        // 预定义常用事件
        fun screenView(screenName: String, screenClass: String? = null) = AnalyticsEvent(
            name = AnalyticsEvents.SCREEN_VIEW,
            parameters = buildMap {
                put("screen_name", AnalyticsValue.Str(screenName))
                screenClass?.let { put("screen_class", AnalyticsValue.Str(it)) }
            }
        )

        fun userLogin(method: String) = AnalyticsEvent(
            name = AnalyticsEvents.USER_LOGIN,
            parameters = mapOf("method" to AnalyticsValue.Str(method))
        )

        fun purchase(
            value: Double,
            currency: String,
            items: List<PurchaseItem>
        ) = AnalyticsEvent(
            name = AnalyticsEvents.PURCHASE,
            value = value,
            currency = currency,
            parameters = mapOf(
                "items_count" to AnalyticsValue.Int(items.size.toLong())
            )
        )
    }
}

/**
 * 购买项（用于电商场景）
 */
data class PurchaseItem(
    val itemId: String,
    val itemName: String,
    val category: String? = null,
    val quantity: Int = 1,
    val price: Double
)
