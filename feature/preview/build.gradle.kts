plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.jb.composeMultiplatform)
    alias(libs.plugins.jb.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.feature.preview.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.preview"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.logger)
            implementation(projects.core.navigation)
            implementation(projects.core.platformCompose)
            implementation(projects.feature.sharing)

            implementation(libs.jb.compose.components.componentsResources)
            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material.materialIconsExtend)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.ui.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.activityCompose)
            implementation(libs.androidx.core.ktx)
        }
    }
}
