plugins {
    alias(libs.plugins.myhub.android)
    // Compose 编译器插件 @Composable
    alias(libs.plugins.jb.composeCompiler)
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
    implementation(projects.composeApp)
    implementation(projects.core.platform)
    implementation(projects.core.startup)

    implementation(libs.koin.android)

    implementation(libs.androidx.activity.activityCompose)
}
