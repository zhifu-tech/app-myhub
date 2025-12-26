package tech.zhifu.app.myhub.system

actual fun getSystemProperty(key: String): String? {
    return System.getProperty(key)
}


