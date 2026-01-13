plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.core.navigation"
    }
    sourceSets {
        commonMain {
            dependencies {
                // Navigation 3（KMP 支持）
                api(libs.jb.androidx.navigation3.navigation3Ui)

                // 状态管理支持（KMP 全平台支持）
                implementation(libs.androidx.savedstate.compose)

                // Kotlin 序列化（NavKey 需要 @Serializable）
                implementation(libs.kotlinx.serialization.json)
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
