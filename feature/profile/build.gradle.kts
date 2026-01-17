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

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.feature.profile.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.profile"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.profileApi)

            implementation(projects.component.mixed)

            implementation(projects.core.logger)
            implementation(projects.core.navigation)
            implementation(projects.core.platform)
            implementation(projects.core.platformCompose)

            implementation(projects.datastore.model)
            implementation(projects.datastore.repositoryClient)

            implementation(libs.jb.compose.components.componentsResources)
            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material.materialIconsExtend)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.ui.ui)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)

            if (project.isDev()) {
                implementation(libs.jb.compose.ui.uiToolingPreview)
            }
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.jb.compose.ui.uiTooling)
}


