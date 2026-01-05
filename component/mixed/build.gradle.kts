plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.component.mixed"
    }

    sourceSets {
        commonMain.dependencies {
            // 平台 Compose 依赖（包含 AppTheme）
            implementation(projects.core.platformCompose)

            // Compose UI 依赖
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            // Preview 支持
            implementation(compose.components.uiToolingPreview)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(compose.uiTooling)
}


