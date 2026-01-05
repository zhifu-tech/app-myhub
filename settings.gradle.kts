rootProject.name = "MyHub"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// ============================================================================
// 插件管理配置
// ============================================================================
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
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
    }
}

// ============================================================================
// 工具链解析插件
// ============================================================================
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// ============================================================================
// 应用模块
// ============================================================================
include(":server")
include(":composeApp")
include(":androidApp")

// ============================================================================
// 核心基础模块
// ============================================================================
include(":core:logger")
include(":core:platform")
include(":core:platform-compose")

// ============================================================================
// 网络模块
// ============================================================================
include(":core:network")
include(":core:network-test")

// ============================================================================
// 统计模块
// ============================================================================
include(":core:analytics")

// ============================================================================
// 数据存储模块
// ============================================================================
// 数据模型
include(":core:datastore-model")

// 数据库模块
include(":core:datastore-database")
include(":core:datastore-database-test")
include(":core:datastore-database-manage")
include(":core:datastore-database-client")
include(":core:datastore-database-server")

// 数据源模块
include(":core:datastore-datasource-local")
include(":core:datastore-datasource-remote")

// 数据仓库模块
include(":core:datastore-repository")
include(":core:datastore-repository-client")
include(":core:datastore-repository-server")

// ============================================================================
// 组件模块
// ============================================================================
include(":component:card")
include(":component:mixed")

// ============================================================================
// 功能模块
// ============================================================================
include(":feature:settings")
include(":feature:dashboard")
include(":feature:profile")
