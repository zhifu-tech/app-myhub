plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.jb.composeCompiler)
    alias(libs.plugins.jb.composeMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.feature.dashboard.api.resource"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.dashboard.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)

            implementation(libs.jb.compose.material.materialIconsExtend)
            implementation(libs.jb.compose.components.componentsResources)

            implementation(libs.jb.androidx.navigation3.navigation3Ui)
        }
    }
}
