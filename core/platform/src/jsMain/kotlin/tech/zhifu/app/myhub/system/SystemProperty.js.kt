package tech.zhifu.app.myhub.system

actual fun getSystemProperty(key: String): String? {
    // JS 平台不支持系统属性，返回 null
    return null
}


