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

dependencies {
    // 项目模块
    implementation(projects.core.platform)

    // Model 模块（用于 API，包含 DTO）
    implementation(projects.core.datastoreModel)

    // Repository 服务端实现
    implementation(projects.core.datastoreRepositoryServer)

    // 数据库模块（通过 datastore-repository-server 传递，但需要显式声明以访问 MyHubDatabase）
    implementation(projects.core.datastoreDatabaseServer)

    // 数据库管理模块（用于初始化数据）
    implementation(projects.core.datastoreDatabaseManage)

    // LocalDataSource（CardRepositoryImpl 需要使用）
    implementation(projects.core.datastoreDatasourceLocal)

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
