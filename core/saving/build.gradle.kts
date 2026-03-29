plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.jb.composeMultiplatform)
    alias(libs.plugins.jb.composeCompiler)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.core.saving"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.activityCompose)
            implementation(libs.androidx.core.ktx)
        }

        webMain.dependencies {
            implementation(libs.jb.compose.ui.ui)
        }
    }
}
