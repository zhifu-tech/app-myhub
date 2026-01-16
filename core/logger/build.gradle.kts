plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.logger"
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.logging)
                implementation(libs.koin.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
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
                implementation(libs.slf4j.api)
                // 使用 Logback（功能强大，支持文件输出和日志轮转）
                implementation(libs.logback)
                
                // 备用：slf4j-simple（简单快速，适合开发）
                // 如果需要切换回 slf4j-simple，取消注释下面这行，并注释掉上面的 logback
                // implementation(libs.slf4j.simple)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.testJunit)
            }
        }
    }
}

