import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

configure<KotlinMultiplatformExtension> {
    applyDefaultHierarchyTemplate()

    android {
        compileSdk = libsCatalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libsCatalog.findVersion("android-minSdk").get().requiredVersion.toInt()

        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosTargets()

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
}
