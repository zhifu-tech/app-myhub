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
                implementation(libs.kotlin.logging)
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
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.testJunit)
            }
        }
    }
}

