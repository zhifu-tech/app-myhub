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
        namespace = "tech.zhifu.app.myhub.feature.sharing"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jb.compose.runtime.runtime)
        }

        androidMain.dependencies {
            implementation(libs.jb.compose.ui.ui)
            implementation(libs.androidx.core.ktx)
        }
    }
}
