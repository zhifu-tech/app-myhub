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

        jvmMain {
            dependencies {
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }


        jvmTest {
            dependencies {
                implementation(libs.kotlin.testJunit)
            }
        }
    }
}

