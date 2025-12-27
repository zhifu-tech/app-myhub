plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.platform"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                // Compose Resources (用于加载资源文件)
                implementation(compose.components.resources)
                // Kotlin Logging (跨平台日志库)
                implementation(libs.kotlin.logging)
                // Koin 依赖注入
                implementation(libs.koin.core)
            }
        }

        androidMain {
            dependencies {
                // Android 平台使用 kotlin-logging-android
                implementation(libs.kotlin.logging.android)
            }
        }

        jvmMain {
            dependencies {
                // JVM 平台使用 slf4j
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }
    }
}
