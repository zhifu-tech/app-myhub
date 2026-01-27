plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.model.dto"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datastore.model)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            // ktor-client-core 包含 HttpStatusCode，可用于异常类
            implementation(libs.ktor.client.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}
