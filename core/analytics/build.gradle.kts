plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.analytics"
    }

    sourceSets {
        commonMain.dependencies {
            // 项目模块依赖
            implementation(projects.core.platform)
            implementation(projects.core.logger)
            implementation(projects.core.appBuildConfig)

            // Kotlinx Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Kotlinx Serialization（用于 FileProvider JSON 输出）
            implementation(libs.kotlinx.serialization.json)

            // Koin（用于 DI 模块）
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            // Firebase Android SDK（仅在 googlePlay 渠道）
            if (project.isChannelGooglePlay()) {
                implementation(libs.firebase.analytics)
                implementation(project.dependencies.platform(libs.firebase.bom))
            }
        }

        iosMain.dependencies {
            // Firebase iOS SDK（仅在 googlePlay 渠道）
            if (project.isChannelGooglePlay()) {
                implementation(libs.firebase.analytics)
            }
        }

        jsMain.dependencies {
            // Firebase JS SDK（仅在 googlePlay 渠道）
            if (project.isChannelGooglePlay()) {
                implementation(libs.firebase.analytics)
            }
        }

        jvmMain.dependencies {
            // JVM 平台特定依赖（FileProvider 需要文件操作）
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}
