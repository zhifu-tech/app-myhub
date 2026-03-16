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
    packageOfResClass = "tech.zhifu.app.myhub.platform.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.platform.compose"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jb.androidx.window.windowCore)
            implementation(libs.jb.compose.components.componentsResources)
            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material3.adaptive.adaptive)
            implementation(libs.jb.compose.material3.adaptive.navigationSuite)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.material3.material3WindowSizeClass)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.ui.ui)

            implementation(libs.orbit.core)
            implementation(libs.orbit.compose)
            implementation(libs.orbit.viewmodel)

            if (project.isDev()) {
                implementation(libs.jb.compose.ui.uiToolingPreview)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.jb.compose.ui.ui)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.jb.compose.ui.uiTooling)
}

