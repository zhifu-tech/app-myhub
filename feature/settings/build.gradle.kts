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
    packageOfResClass = "tech.zhifu.app.myhub.feature.settings.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.settings"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.logger)
            implementation(projects.core.platform)
            implementation(projects.core.platformCompose)

            implementation(projects.datastore.model)
            implementation(projects.datastore.repositoryClientApi)

            implementation(libs.jb.compose.components.componentsResources)
            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material.materialIconsExtend)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.ui.ui)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)

            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)

            if (project.isDev()) {
                implementation(libs.jb.compose.ui.uiToolingPreview)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.jb.compose.ui.uiTooling)
}
