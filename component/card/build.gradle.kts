plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.component.card"
    }

    sourceSets {
        commonMain.dependencies {
            // 数据模型依赖
            implementation(projects.core.datastoreModel)
            // kotlinx-datetime 用于日期格式化
            implementation(libs.kotlinx.datetime)
            // Compose UI 依赖
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            // Material Icons 扩展（必需：Icons.Default.* 图标）
            implementation(compose.materialIconsExtended)
            // 依赖注入
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
        }
    }
}

