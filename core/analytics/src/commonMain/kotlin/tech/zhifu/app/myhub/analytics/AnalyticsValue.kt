package tech.zhifu.app.myhub.analytics

/**
 * 类型安全的统计值类型
 *
 * 原因：避免使用 Any 导致的跨平台兼容性问题
 * - JS/WASM 不支持所有 Any 类型
 * - Firebase 只接受 String / Long / Double / Boolean
 * - iOS SDK 也有类型限制
 */
sealed interface AnalyticsValue {
    data class Str(val value: String) : AnalyticsValue
    data class Num(val value: Double) : AnalyticsValue
    data class Int(val value: Long) : AnalyticsValue
    data class Bool(val value: Boolean) : AnalyticsValue

    companion object {
        fun from(value: Any?): AnalyticsValue? = when (value) {
            is String -> Str(value)
            is Boolean -> Bool(value)
            is Long -> Int(value)
            is Double -> Num(value)
            is Float -> Num(value.toDouble())
            null -> null
            is Number -> {
                // 统一处理所有数字类型（兼容 JS 平台）
                val doubleValue = value.toDouble()
                // 检查是否为整数
                val longValue = doubleValue.toLong()
                if (doubleValue == longValue.toDouble()) {
                    // 是整数，使用 Int
                    Int(longValue)
                } else {
                    // 是浮点数，使用 Num
                    Num(doubleValue)
                }
            }
            else -> Str(value.toString()) // 兜底：转换为字符串
        }
    }
}
