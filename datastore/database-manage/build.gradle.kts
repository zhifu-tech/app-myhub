plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
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
    }

    iosTargets().forEach { iosTarget ->
        iosTarget.binaries.all {
            linkerOpts += listOf("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // 平台模块（提供 ResourceLoader）
            implementation(projects.core.platform)

            // 数据库模块
            implementation(projects.datastore.database)

            // 数据模型
            implementation(projects.datastore.model)

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
        wasmJsMain.dependencies {
            // SQLDelight Web 驱动（WASM 平台）
            implementation(libs.sqldelight.web)
        }
    }
}

