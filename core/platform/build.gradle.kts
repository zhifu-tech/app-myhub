plugins {
    alias(libs.plugins.myhub.kmp)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.platform"
    }

    sourceSets {
        commonMain {
            dependencies {
                // Logger 模块
                implementation(projects.core.logger)
                // Koin 依赖注入
                implementation(libs.koin.core)
            }
        }
    }
}
