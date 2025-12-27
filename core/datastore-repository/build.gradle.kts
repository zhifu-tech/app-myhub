plugins {
    alias(libs.plugins.myhub.kmp)
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

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

