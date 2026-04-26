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
    packageOfResClass = "tech.zhifu.app.myhub.feature.ai.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.ai"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.component.media)
            implementation(projects.component.static)
            implementation(projects.core.analytics)
            implementation(projects.core.logger)
            implementation(projects.core.network)
            implementation(projects.core.navigation)
            implementation(projects.core.startup)
            implementation(projects.datastore.model)
            implementation(projects.datastore.fileStorage)
            implementation(projects.datastore.repositoryClientApi)
            implementation(projects.feature.aiApi)
            implementation(projects.feature.settingsApi)
            implementation(projects.feature.preview)
            implementation(projects.ui.design)
            implementation(projects.ui.state)

            implementation(libs.jb.androidx.window.windowCore)
            implementation(libs.jb.compose.components.componentsResources)
            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material.materialIconsExtend)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.material3.adaptive.adaptive)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.ui.ui)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.orbit.core)
            implementation(libs.orbit.compose)
            implementation(libs.orbit.viewmodel)
        }
    }
}
