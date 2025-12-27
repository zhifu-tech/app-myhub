plugins {
    alias(libs.plugins.myhub.kmp)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.database.test"
        withHostTest {
            isIncludeAndroidResources = false
        }
    }

    iosTargets().forEach { iosTarget ->
        iosTarget.binaries.all {
            linkerOpts += listOf("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.datastoreDatabase)
            implementation(libs.kotlinx.coroutines.test)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.sqlite)
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
            implementation(npm("sql.js", "1.12.0"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }
    }
}

