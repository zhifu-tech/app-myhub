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
                implementation(libs.jb.androidx.savedstate.compose)

                // Kotlin 序列化（NavKey 需要 @Serializable）
                implementation(libs.kotlinx.serialization.json)

                // Material3 组件库（必需：Material Design 3 组件）
                implementation(compose.material3)
                implementation(libs.jb.compose.material3.adaptive.adaptive)

                // Material3 Adaptive Navigation Suite（必需：Adaptive Navigation 组件）
                implementation(compose.material3AdaptiveNavigationSuite)
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
