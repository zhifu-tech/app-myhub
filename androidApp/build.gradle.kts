import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Android 应用插件
    alias(libs.plugins.androidApplication)
    // Kotlin Android 插件
    kotlin("android")
    // Compose 编译器插件 @Composable
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "tech.zhifu.app.myhub.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "tech.zhifu.app.myhub"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
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

    // ========== Android 平台特定依赖 ==========
    // Koin Android 扩展（必需：MyHubApplication 使用 androidContext）
    // 注意：composeApp 已经包含了 koin-core，这里只需要 Android 扩展
    implementation(libs.koin.android)

    // Activity Compose 集成（必需：MainActivity 使用 setContent 和 enableEdgeToEdge）
    // 注意：这是 Android 特定的，composeApp 是跨平台的，所以需要在这里单独添加
    implementation(libs.androidx.activity.compose)

    // Compose Preview 工具（必需：MainActivity 使用 @Preview）
    // 注意：使用 Android 特定的 Preview，不是 Compose Multiplatform 的 Preview
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // ========== 测试依赖 ==========
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
