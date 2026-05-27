package tech.zhifu.app.myhub.system

actual fun getSystemProperty(key: String): String? {
    // Web 平台（JS/WASM）不支持系统属性，返回 null
    return null
}

