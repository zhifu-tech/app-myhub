plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.components.resources)
                // Material3 依赖（Theme.kt 需要）
                implementation(compose.material3)
                // Foundation 依赖（isSystemInDarkTheme 需要）
                implementation(compose.foundation)
            }
        }
    }
}

