plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader

    // Kotlin & KMP 核心插件
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false

    // Compose 相关
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false // Kotlin 2.0+ 推荐显式声明
    alias(libs.plugins.composeHotReload) apply false

    // Android 插件
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false

    // 其他功能插件
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.sqldelight) apply false

    // myhub
    alias(libs.plugins.myhub.kmp) apply false
}
