plugins {
    alias(libs.plugins.myhub.android)
    // Compose 编译器插件 @Composable
    alias(libs.plugins.composeCompiler)
}

if (project.isChannelGooglePlay()) {
    // 使用 get().pluginId 获取 ID
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

android {
    namespace = "tech.zhifu.app.myhub.app"

    defaultConfig {
        applicationId = "tech.zhifu.app.myhub"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    // ========== 项目模块依赖 ==========
    // 主应用模块（包含所有 Compose UI 和业务逻辑）
    // 注意：composeApp 已经包含了 Compose Multiplatform、Koin Core、数据层等
    implementation(projects.composeApp)
    implementation(projects.core.platform)

    // ========== Android 平台特定依赖 ==========
    // Koin Android 扩展（必需：MyHubApplication 使用 androidContext）
    // 注意：composeApp 已经包含了 koin-core，这里只需要 Android 扩展
    implementation(libs.koin.android)


    implementation(libs.androidx.activity.activityCompose)

    implementation(libs.androidx.compose.ui.uiToolingPreview)
    debugImplementation(libs.androidx.compose.ui.uiTooling)
}
