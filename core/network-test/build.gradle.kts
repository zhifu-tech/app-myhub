plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.network.test"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.network)
            // Ktor Client Mock for testing
            api(libs.ktor.client.mock)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
