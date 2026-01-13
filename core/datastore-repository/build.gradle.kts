plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.repository"
    }

    sourceSets {
        commonMain.dependencies {
            // 模型类型
            implementation(projects.core.datastoreModel)

            // Kotlinx Coroutines（Flow 需要）
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

