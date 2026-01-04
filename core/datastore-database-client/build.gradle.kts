plugins {
    alias(libs.plugins.myhub.kmp)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.database.client"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.datastoreDatabase)
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
//            implementation(npm("sql.js", "1.12.0"))
//            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
//            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }

        wasmJsMain.dependencies {
            // SQLDelight Web 驱动（WASM 平台）
            implementation(libs.sqldelight.web)
            implementation(libs.ktor.client.js)
//            implementation(npm("sql.js", "1.12.0"))
//            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
//            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }
    }
}

