package tech.zhifu.app.myhub.analytics

/**
 * 统计事件命名规范
 * 统一管理事件名称，避免字符串散落，便于维护和重构
 */
object AnalyticsEvents {
    // 页面浏览事件
    const val SCREEN_VIEW = "screen_view"

    // 用户相关事件
    const val USER_LOGIN = "user_login"
    const val USER_LOGOUT = "user_logout"
    const val USER_REGISTER = "user_register"

    // 业务事件（示例）
    const val CARD_CREATED = "card_created"
    const val CARD_UPDATED = "card_updated"
    const val CARD_DELETED = "card_deleted"

    // 电商事件（示例）
    const val PURCHASE = "purchase"
    const val ADD_TO_CART = "add_to_cart"
    const val REMOVE_FROM_CART = "remove_from_cart"
}
