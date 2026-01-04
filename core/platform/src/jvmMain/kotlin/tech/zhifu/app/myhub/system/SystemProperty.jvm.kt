package tech.zhifu.app.myhub.system

actual fun getSystemProperty(key: String): String? {
    // JVM 平台的 System.getProperty() 不允许空 key，会抛出 IllegalArgumentException
    // 为了保持跨平台一致性，空 key 直接返回 null
    if (key.isEmpty()) {
        return null
    }
    return System.getProperty(key)
}
