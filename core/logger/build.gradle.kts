plugins {
    alias(libs.plugins.myhub.kmp)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.logger"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.logging)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }
    }
}

