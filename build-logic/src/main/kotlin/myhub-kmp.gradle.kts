import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

configure<KotlinMultiplatformExtension> {
    applyDefaultHierarchyTemplate()

    // 1. 解析当前激活的变体组件（独立参数）
    val env = project.getVariantEnvironment()
    val tier = project.getVariantTier()
    val channel = project.getVariantChannel()
    val envTitle = env.replaceFirstChar { it.uppercase() }
    val tierTitle = tier.replaceFirstChar { it.uppercase() }
    val channelTitle = channel.replaceFirstChar { it.uppercase() }

    sourceSets {
        // 2. 注入业务逻辑 (Common)
        commonMain.get().apply {
            // 注入环境目录 (e.g., src/devMain)
            kotlin.srcDir("src/${env}Main/kotlin")
            // 注入级别目录 (e.g., src/freeMain)
            kotlin.srcDir("src/${tier}Main/kotlin")
            // 注入渠道目录 (e.g., src/googlePlayMain)，如果指定了渠道
            channel.let {
                kotlin.srcDir("src/${it}Main/kotlin")
            }

            resources.srcDir("src/${env}Main/resources")
            resources.srcDir("src/${tier}Main/resources")
            channel.let {
                resources.srcDir("src/${it}Main/resources")
            }
        }
    }
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        optIn.add("dev.chrisbanes.haze.ExperimentalHazeApi")
        optIn.add("dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
        optIn.add("org.mobilenativefoundation.store.core5.ExperimentalStoreApi")
    }
}
