plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jb.composeCompiler)
    alias(libs.plugins.jb.composeMultiplatform)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.core.navigation"
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.jb.androidx.navigation3.navigation3Ui)

                implementation(libs.jb.androidx.savedstate.compose)

                implementation(libs.jb.compose.material3.adaptive.navigationSuite)
                implementation(libs.jb.compose.material3.material3)

                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.jb.androidx.navigation3.navigation3Ui)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.testJunit)
            }
        }
    }
}
