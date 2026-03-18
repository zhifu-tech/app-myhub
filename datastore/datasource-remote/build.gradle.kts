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
            implementation(projects.core.appBuildConfig)
            implementation(projects.core.logger)
            implementation(projects.core.network)
            implementation(projects.core.platform)

            implementation(projects.datastore.model)
            implementation(projects.datastore.modelDto)
            implementation(projects.datastore.sync)

            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        commonTest.dependencies {
            implementation(projects.core.networkTest)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
