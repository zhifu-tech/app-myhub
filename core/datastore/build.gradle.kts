import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        // 配置主机端测试（单元测试）
        withHostTest {
            isIncludeAndroidResources = false
        }
    }

    jvm()

    js {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Kotlin 标准库
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // SQLDelight
            implementation(libs.sqldelight.coroutines)

            // Koin
            implementation(libs.koin.core)

            // Core modules
            implementation(projects.core.platform)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.sqldelight.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android)
            implementation(libs.ktor.client.android)
        }

        val androidHostTest by getting {
            dependencies {
                implementation(libs.androidx.testExt.junit)
                implementation(libs.sqldelight.sqlite)
            }
        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite)
            implementation(libs.ktor.client.cio)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native)
            implementation(libs.ktor.client.darwin)
        }

        jsMain.dependencies {
            implementation(libs.sqldelight.web)
            implementation(libs.ktor.client.js)
            implementation(npm("sql.js", "1.12.0"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }

        jsTest.dependencies {
            implementation(libs.sqldelight.coroutines)
        }
    }
}

sqldelight {
    databases {
        create("MyHubDatabase") {
            packageName.set("tech.zhifu.app.myhub.datastore.database")
            generateAsync.set(true)
        }
    }
    linkSqlite = true
}
