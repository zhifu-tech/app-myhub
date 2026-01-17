plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.myhub.kmp.web)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.database.client"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datastore.database)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android)
        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }

        jsMain.dependencies {
            implementation(libs.sqldelight.web)
            implementation(libs.ktor.client.js)
        }

        wasmJsMain.dependencies {
            implementation(libs.sqldelight.web)
            implementation(libs.ktor.client.js)
        }
    }
}

