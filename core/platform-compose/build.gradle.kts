plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.platform.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.platform.compose"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.components.resources)
                // Material3 依赖（Theme.kt 需要）
                implementation(compose.material3)
                // Foundation 依赖（isSystemInDarkTheme 需要）
                implementation(compose.foundation)
                implementation(libs.jb.compose.material3.adaptive.adaptive)
                implementation(libs.jb.compose.material3.material3WindowSizeClass)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                // Compose UI 依赖（测试需要 DpSize 等类型）
                implementation(compose.ui)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.testJunit)
            }
        }
    }
}

