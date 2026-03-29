rootProject.name = "MyHub"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// ============================================================================
// 插件管理配置
// ============================================================================
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// ============================================================================
// 依赖解析管理配置
// ============================================================================
dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jogamp.org/deployment/maven/")
    }
}

// ============================================================================
// 工具链解析插件
// ============================================================================
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// ============================================================================
// 平台配置
// ============================================================================
val allPlatforms = setOf("android", "ios", "jvm", "js", "wasmJs", "server")
val enabledPlatforms = providers.gradleProperty("enabledPlatforms")
    .orElse("").get().trim().let { platforms ->
        if (platforms.isBlank()) {
            allPlatforms
        } else {
            platforms.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it in allPlatforms }
                .toSet()
        }
    }
val isAllPlatformsEnabled = enabledPlatforms.containsAll(allPlatforms)
val isAndroidEnabled = enabledPlatforms.contains("android")
val isIosEnabled = enabledPlatforms.contains("ios")
val isJvmEnabled = enabledPlatforms.contains("jvm")
val isJsEnabled = enabledPlatforms.contains("js")
val isWasmJsEnabled = enabledPlatforms.contains("wasmJs")
val isServerEnabled = enabledPlatforms.contains("server")

// 格式化输出 平台启用信息
println(
    """
    ===========================================
    平台配置:
      enabledPlatforms: $enabledPlatforms
      isAllPlatformsEnabled: $isAllPlatformsEnabled
      isAndroidEnabled: $isAndroidEnabled
      isIosEnabled: $isIosEnabled
      isJvmEnabled: $isJvmEnabled
      isJsEnabled: $isJsEnabled
      isWasmJsEnabled: $isWasmJsEnabled
      isServerEnabled: $isServerEnabled
    ===========================================
    """.trimIndent()
)

// 验证启用的平台是否合法
// 规则：
// 1. server 启用的前提是，必须要有 jvm
// 2. 可以接受的组合为：
//    - 单个平台：android, ios, jvm, js, wasmJs
//    - 两个平台： jvm + server

// 首先检查：如果启用了 server，必须同时启用 jvm
if (isServerEnabled && !isJvmEnabled) {
    throw GradleException(
        """
    Illegal enabledPlatforms combination:
    当前配置: $enabledPlatforms
    server 启用的前提是，必须要有 jvm。
    具体配置方式请检查 'enabledPlatforms' gradle 属性。
""".trimIndent()
    )
}

// 然后检查组合是否合法
val validSinglePlatforms = setOf("android", "ios", "jvm", "js", "wasmJs")
when (enabledPlatforms.size) {
    6 -> println("all platforms enabled")
    1 -> {
        // 只启用一个平台，必须是有效的单个平台
        if (enabledPlatforms.first() !in validSinglePlatforms) {
            throw GradleException(
                """
            Illegal enabledPlatforms combination:
            当前配置: $enabledPlatforms
            单个平台必须是以下之一: android, ios, jvm, js, wasmJs
            具体配置方式请检查 'enabledPlatforms' gradle 属性。
    """.trimIndent()
            )
        }
    }

    2 -> {
        // 启用两个平台，必须是：jvm + server
        if (!enabledPlatforms.contains("jvm") || !enabledPlatforms.contains("server") || enabledPlatforms.size != 2) {
            throw GradleException(
                """
            Illegal enabledPlatforms combination:
            当前配置: $enabledPlatforms
            两个平台必须正好是 jvm 和 server
            具体配置方式请检查 'enabledPlatforms' gradle 属性。
    """.trimIndent()
            )
        }
    }

    else -> throw GradleException(
        """
    Illegal enabledPlatforms combination:
    当前配置: $enabledPlatforms
    只允许
      1) 仅启用一个平台（android, ios, jvm, js, wasmJs）；
      2) 启用两个平台（jvm, server）。
    具体配置方式请检查 'enabledPlatforms' gradle 属性。
""".trimIndent()
    )
}

// 传递平台配置给 子项目访问
gradle.beforeProject {
    if (project == rootProject) {
        project.extensions.extraProperties.set("isAndroidEnabled", isAndroidEnabled)
        project.extensions.extraProperties.set("isIosEnabled", isIosEnabled)
        project.extensions.extraProperties.set("isJvmEnabled", isJvmEnabled)
        project.extensions.extraProperties.set("isJsEnabled", isJsEnabled)
        project.extensions.extraProperties.set("isWasmJsEnabled", isWasmJsEnabled)
        project.extensions.extraProperties.set("isServerEnabled", isServerEnabled)
    }
}


// ============================================================================
// 核心基础模块
// ============================================================================
include(":core:logger")
include(":core:cache")
include(":core:app-build-config")
include(":core:platform")
include(":core:navigation")
include(":core:settings")
include(":core:startup")

// 网络模块
include(":core:network")
include(":core:network-test")
include(":core:sharing")
include(":core:saving")

// 统计模块
include(":core:analytics")

// ============================================================================
// 数据存储模块
// ============================================================================
include(":datastore:model")
include(":datastore:model-dto")

// 数据库模块
include(":datastore:database")
include(":datastore:database-test")
include(":datastore:database-client")
if (isServerEnabled) {
    include(":datastore:database-server")
}

// Bootstrap 模块
include(":datastore:bootstrap")

// 数据源模块
include(":datastore:datasource-local")
include(":datastore:datasource-remote")

// 数据仓库模块
include(":datastore:repository-client-api")
include(":datastore:repository-client")
if (isServerEnabled) {
    include(":datastore:repository-server-api")
    include(":datastore:repository-server")
}

// 同步模块
include(":datastore:sync")

// ============================================================================
// 组件模块
// ============================================================================
include(":component:media")

// ============================================================================
// UI 模型模块
// ============================================================================
include(":ui:design")
include(":ui:model")

// ============================================================================
// 功能模块
// ============================================================================
include(":feature:dashboard-api")
include(":feature:dashboard")
include(":feature:mixed-api")
include(":feature:mixed")
include(":feature:preview")

// ============================================================================
// 应用模块
// ============================================================================
include(":composeApp")
if (isAndroidEnabled) {
    include(":androidApp")
}
if (isServerEnabled) {
    include(":server")
}

// ============================================================================
// 构建文件动态生成系统
// ============================================================================
// 应用构建文件生成器脚本插件（加载辅助类和函数）
apply(from = "gradle/build-file-generator.gradle.kts")

// 从 extra 属性中获取函数
@Suppress("UNCHECKED_CAST")
val generateAndSetBuildFile =
    extensions.extraProperties["generateAndSetBuildFile"] as (ProjectDescriptor, String, ProjectDescriptor) -> Unit

// 递归设置所有项目的构建文件
fun configPlatformSpecificBuildFile(project: ProjectDescriptor) {
    when {
        isAllPlatformsEnabled -> {
            // 所有平台启用，使用默认构建文件
            project.buildFileName = "build.gradle.kts"
        }

        isAndroidEnabled -> generateAndSetBuildFile(project, "android", rootProject)
        isIosEnabled -> generateAndSetBuildFile(project, "ios", rootProject)
        isJvmEnabled -> generateAndSetBuildFile(project, "jvm", rootProject)
        isJsEnabled -> generateAndSetBuildFile(project, "js", rootProject)
        isWasmJsEnabled -> generateAndSetBuildFile(project, "wasmJs", rootProject)
        isServerEnabled -> Unit
    }
    project.children.forEach { configPlatformSpecificBuildFile(it) }
}

configPlatformSpecificBuildFile(rootProject)
