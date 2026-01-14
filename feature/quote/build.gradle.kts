plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.quote"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.quoteApi)
            implementation(libs.androidx.navigation3.navigation3Runtime)
            implementation(libs.jb.androidx.navigation3.navigation3Ui)
            implementation(libs.jb.compose.material3.adaptive.adaptive)
            implementation(libs.jb.compose.material3.adaptive.adaptiveLayout)
            implementation(libs.jb.compose.material3.adaptive.adaptiveNavigation)
            implementation(libs.jb.compose.material3.adaptive.adaptiveNavigation)

            implementation(libs.jb.compose.material3.material3WindowSizeClass)
        }
    }
}
