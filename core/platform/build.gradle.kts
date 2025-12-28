plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.platform"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                // Compose Resources (用于加载资源文件)
                implementation(compose.components.resources)
                // Logger 模块
                implementation(projects.core.logger)
                // Koin 依赖注入
                implementation(libs.koin.core)
            }
        }
    }
}
