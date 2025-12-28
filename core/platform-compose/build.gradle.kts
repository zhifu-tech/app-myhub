plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            }
        }
    }
}

