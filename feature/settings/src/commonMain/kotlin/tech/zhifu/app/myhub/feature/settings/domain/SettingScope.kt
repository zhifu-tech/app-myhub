package tech.zhifu.app.myhub.feature.settings.domain

/**
 * 设置项作用域
 */
enum class SettingScope {
    /**
     * 应用级设置（所有用户共享）
     * 存储在本地，不随用户切换而变化
     */
    APP,

    /**
     * 用户级设置（每个用户独立）
     * 存储在用户偏好中，随用户切换而变化
     */
    USER,

    /**
     * 会话级设置（运行时临时）
     * 仅存储在内存中，不持久化
     */
    SESSION
}


