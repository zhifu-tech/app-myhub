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
    sourceSets {
        // ========== 变体 Source Sets ==========
        // 开发环境变体
        val devFreeMain by creating {
            dependsOn(commonMain.get())
        }
        val devPremiumMain by creating {
            dependsOn(commonMain.get())
        }

        // 生产环境变体
        val prodFreeMain by creating {
            dependsOn(commonMain.get())
        }
        val prodPremiumMain by creating {
            dependsOn(commonMain.get())
        }

        // Android 平台变体
        val androidDevFreeMain by creating {
            dependsOn(devFreeMain)
            dependsOn(androidMain.get())
        }
        val androidDevPremiumMain by creating {
            dependsOn(devPremiumMain)
            dependsOn(androidMain.get())
        }
        val androidProdFreeMain by creating {
            dependsOn(prodFreeMain)
            dependsOn(androidMain.get())
        }
        val androidProdPremiumMain by creating {
            dependsOn(prodPremiumMain)
            dependsOn(androidMain.get())
        }

        // iOS 平台变体（使用默认配置，可通过编译标志区分）
        val iosDevFreeMain by creating {
            dependsOn(devFreeMain)
            dependsOn(iosMain.get())
        }
        val iosDevPremiumMain by creating {
            dependsOn(devPremiumMain)
            dependsOn(iosMain.get())
        }
        val iosProdFreeMain by creating {
            dependsOn(prodFreeMain)
            dependsOn(iosMain.get())
        }
        val iosProdPremiumMain by creating {
            dependsOn(prodPremiumMain)
            dependsOn(iosMain.get())
        }

        // JVM 平台变体
        val jvmDevFreeMain by creating {
            dependsOn(devFreeMain)
            dependsOn(jvmMain.get())
        }
        val jvmDevPremiumMain by creating {
            dependsOn(devPremiumMain)
            dependsOn(jvmMain.get())
        }
        val jvmProdFreeMain by creating {
            dependsOn(prodFreeMain)
            dependsOn(jvmMain.get())
        }
        val jvmProdPremiumMain by creating {
            dependsOn(prodPremiumMain)
            dependsOn(jvmMain.get())
        }

        // JS 平台变体
        val jsDevFreeMain by creating {
            dependsOn(devFreeMain)
            dependsOn(jsMain.get())
        }
        val jsDevPremiumMain by creating {
            dependsOn(devPremiumMain)
            dependsOn(jsMain.get())
        }
        val jsProdFreeMain by creating {
            dependsOn(prodFreeMain)
            dependsOn(jsMain.get())
        }
        val jsProdPremiumMain by creating {
            dependsOn(prodPremiumMain)
            dependsOn(jsMain.get())
        }
    }
}
