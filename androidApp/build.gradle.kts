import java.util.Properties

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

    signingConfigs {
        create("release") {
            val releaseConfig = Properties().apply {
                val configFile = rootProject.file("androidApp/release.properties")
                if (configFile.exists()) {
                    configFile.inputStream().use { load(it) }
                }
            }

            fun releaseProp(name: String): String? {
                val gradleValue = project.findProperty(name)?.toString()
                if (!gradleValue.isNullOrBlank()) return gradleValue
                val fileValue = releaseConfig.getProperty(name)
                return fileValue?.takeIf { it.isNotBlank() }
            }

            val keystorePath = releaseProp("releaseKeystorePath")
            val keystorePassword = releaseProp("releaseKeystorePassword")
            val keyAlias = releaseProp("releaseKeyAlias")
            val keyPassword = releaseProp("releaseKeyPassword")

            if (
                !keystorePath.isNullOrBlank() &&
                !keystorePassword.isNullOrBlank() &&
                !keyAlias.isNullOrBlank() &&
                !keyPassword.isNullOrBlank()
            ) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
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
