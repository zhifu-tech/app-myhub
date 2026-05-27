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
        namespace = "tech.zhifu.app.myhub.component.media"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.logger)
            implementation(projects.ui.design)

            implementation(libs.coil.compose)
            implementation(libs.compose.multiplatform.media.player)
            api(libs.filekit.core)
            api(libs.filekit.coil)
            api(libs.filekit.dialogs)
            api(libs.filekit.dialogs.compose)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.ui.ui)
            implementation(libs.jb.compose.material.materialIconsExtend)
            implementation(libs.koin.compose)
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
