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
        versionName = "0.1.0"
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

tasks.register<Copy>("renameReleaseApk") {
    dependsOn("assembleRelease")
    val appEnv = project.findProperty("appEnv")?.toString()?.takeIf { it.isNotBlank() } ?: "dev"
    val appTier = project.findProperty("appTier")?.toString()?.takeIf { it.isNotBlank() } ?: "free"
    val appChannel = project.findProperty("appChannel")?.toString()?.takeIf { it.isNotBlank() } ?: "channel"
    val versionName = android.defaultConfig.versionName ?: "1.0.0"
    val outputDir = layout.buildDirectory.dir("outputs/apk/release")
    val targetFileName = "myhub-${appEnv}-${appTier}-${appChannel}-${versionName}.apk"

    from(outputDir) {
        include("androidApp-release.apk")
    }
    into(outputDir)
    rename { targetFileName }
}

tasks.register<Delete>("cleanReleaseApkName") {
    dependsOn("renameReleaseApk")
    delete(layout.buildDirectory.file("outputs/apk/release/androidApp-release.apk"))
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    finalizedBy("renameReleaseApk", "cleanReleaseApkName")
}

tasks.matching { it.name == "renameReleaseApk" }.configureEach {
    mustRunAfter("createReleaseApkListingFileRedirect")
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.core.platform)
    implementation(projects.core.startup)

    implementation(libs.koin.android)

    implementation(libs.androidx.activity.activityCompose)
}
