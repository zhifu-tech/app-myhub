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
        namespace = "tech.zhifu.app.myhub.datastore.datasource.remote"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.platform)
            implementation(projects.core.logger)
            implementation(projects.core.datastoreModel)
            implementation(projects.core.network)

            // Kotlinx Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // Koin（用于 RemoteDataSourceModule）
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

            // Model for test data
            implementation(projects.core.datastoreModel)

            // Network test utilities
            implementation(projects.core.networkTest)
        }
    }
}

