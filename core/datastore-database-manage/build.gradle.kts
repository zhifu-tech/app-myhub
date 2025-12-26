import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.datastore.database.manage.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.database.manage"
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
        commonMain.dependencies {
            // 平台模块（提供 ResourceLoader）
            implementation(projects.core.platform)

            // 数据库模块
            implementation(projects.core.datastoreDatabase)

            // 数据模型
            implementation(projects.core.datastoreModel)

            // 序列化
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // 协程
            implementation(libs.kotlinx.coroutines.core)

            // 依赖注入
            implementation(libs.koin.core)

            // Compose Resources
            implementation(compose.runtime)
            implementation(compose.components.resources)
        }

        androidMain.dependencies {
            // SQLDelight Android 驱动
            implementation(libs.sqldelight.android)
        }

        jvmMain.dependencies {
            // SQLDelight SQLite 驱动（用于 JVM/Server）
            implementation(libs.sqldelight.sqlite)
        }

        iosMain.dependencies {
            // SQLDelight Native 驱动
            implementation(libs.sqldelight.native)
        }

        jsMain.dependencies {
            // SQLDelight Web 驱动
            implementation(libs.sqldelight.web)
        }
    }
}

