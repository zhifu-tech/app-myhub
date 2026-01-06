import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")
}

configure<ApplicationExtension> {

    compileSdk = libsCatalog.findVersion("android-compileSdk").get().requiredVersion.toInt()

    defaultConfig {
        minSdk = libsCatalog.findVersion("android-minSdk").get().requiredVersion.toInt()
        targetSdk = libsCatalog.findVersion("android-targetSdk").get().requiredVersion.toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // ========== 变体配置（与 KMP 一致）==========
    // 解析当前激活的变体组件（独立参数，与 KMP 系统一致）
    val env = project.getVariantEnvironment()
    val tier = project.getVariantTier()
    val channel = project.getVariantChannel()

    // 动态注入源集目录（与 KMP 的源集注入方式一致）
    sourceSets {
        named("main") {
            // 注入环境目录 (e.g., src/dev/)
            java.directories.add("src/${env}/java")
            kotlin.directories.add("src/${env}/kotlin")
            res.directories.add("src/${env}/res")
            // AndroidManifest.xml 会自动合并，不需要手动指定

            // 注入级别目录 (e.g., src/free/)
            java.directories.add("src/${tier}/java")
            kotlin.directories.add("src/${tier}/kotlin")
            res.directories.add("src/${tier}/res")
            // AndroidManifest.xml 会自动合并（如果存在）

            // 注入渠道目录 (e.g., src/googlePlay/)，如果指定了渠道且不是默认渠道
            java.directories.add("src/${channel}/java")
            kotlin.directories.add("src/${channel}/kotlin")
            res.directories.add("src/${channel}/res")
            // AndroidManifest.xml 会自动合并（如果存在）
        }
    }
}

// 配置排除规则（与 androidApp/build.gradle.kts 中的配置一致）
configurations.all {
    // 排除 kotlin-logging-android-debug，避免与 kotlin-logging-android 的重复类错误
    exclude(group = "io.github.oshai", module = "kotlin-logging-android-debug")
}
