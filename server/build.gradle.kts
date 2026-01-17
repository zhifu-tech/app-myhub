plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "tech.zhifu.app.myhub"
version = "1.0.0"
application {
    mainClass.set("tech.zhifu.app.myhub.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // 项目模块
    implementation(projects.core.logger)

    // Model 模块（用于 API，包含 DTO）
    implementation(projects.datastore.model)

    // Repository 服务端实现
    implementation(projects.datastore.repositoryServer)

    // 数据库模块（通过 datastore-repository-server 传递，但需要显式声明以访问 MyHubDatabase）
    implementation(projects.datastore.databaseServer)

    // 数据库管理模块（用于初始化数据）
    implementation(projects.datastore.databaseManage)

    // LocalDataSource（CardRepositoryImpl 需要使用）
    implementation(projects.datastore.datasourceLocal)

    // Ktor Server
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serverStatusPages)

    // 序列化
    implementation(libs.kotlinx.serialization.json)

    // 日志
    implementation(libs.logback)

    // 依赖注入
    implementation(libs.koin.core)

    // 测试
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
