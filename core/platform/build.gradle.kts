import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}



kotlin {
    androidLibrary {
        namespace = "tech.zhifu.app.myhub.platform"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    js {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                // Compose Resources (用于加载资源文件)
                implementation(compose.components.resources)
                // Kotlin Logging (跨平台日志库)
                implementation(libs.kotlin.logging)
                // Koin 依赖注入
                implementation(libs.koin.core)
            }
        }

        androidMain {
            dependencies {
                // Android 平台使用 kotlin-logging-android
                implementation(libs.kotlin.logging.android)
            }
        }

        jvmMain {
            dependencies {
                // JVM 平台使用 slf4j
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }
    }
}
