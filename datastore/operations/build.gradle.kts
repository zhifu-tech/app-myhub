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
        namespace = "tech.zhifu.app.myhub.datastore.operations"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datastore.model)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
