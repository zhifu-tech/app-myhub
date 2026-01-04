package tech.zhifu.app.myhub.logger

private var hasSetSystemProperty = false

internal actual fun LoggerConfig.configPlatform() {
    if (useAndroidLogger) {
        if (hasSetSystemProperty.not()) {
            hasSetSystemProperty = true
            System.setProperty("kotlin-logging-to-android-native", "true")
        }
    }
}
