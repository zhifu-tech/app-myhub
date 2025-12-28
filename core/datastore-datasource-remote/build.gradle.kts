plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.datasource.remote"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.logger)
            implementation(projects.core.datastoreModel)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Kotlinx Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin（用于 RemoteDataSourceModule）
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

            // Ktor Client Mock for testing
            implementation(libs.ktor.client.mock)

            // Model for test data
            implementation(projects.core.datastoreModel)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

