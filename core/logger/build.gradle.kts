plugins {
    alias(libs.plugins.myhub.kmp)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.logger"
    }

    sourceSets {
        commonMain {
            dependencies {
                // Kotlin Logging (跨平台日志库)
                implementation(libs.kotlin.logging)
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

