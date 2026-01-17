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
        namespace = "tech.zhifu.app.myhub.component.mixed"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.platformCompose)

            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.ui.ui)

            if (project.isDev()) {
                implementation(libs.jb.compose.ui.uiToolingPreview)
            }
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.jb.compose.ui.uiTooling)
}


