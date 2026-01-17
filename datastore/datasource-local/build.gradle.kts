plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.myhub.kmp.web)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.datasource.local"
    }

    iosTargets().forEach { iosTarget ->
        iosTarget.binaries.all {
            linkerOpts += listOf("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datastore.model)
            implementation(projects.datastore.database)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.sqldelight.coroutines)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

            // 数据库测试工具（runDatabaseTest）
            implementation(projects.datastore.databaseTest)
        }
    }
}

