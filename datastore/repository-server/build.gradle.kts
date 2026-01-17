plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Logger 模块
    implementation(projects.core.logger)

    // Repository 接口
    api(projects.datastore.repository)

    implementation(projects.datastore.database)
    // 数据库服务端模块（MyHubDatabase）
    implementation(projects.datastore.databaseServer)

    // LocalDataSource（复用客户端实现，避免代码重复）
    implementation(projects.datastore.datasourceLocal)

    // Model（包含 DTO，用于 Service 层转换）
    implementation(projects.datastore.model)

    // SQLDelight
    implementation(libs.sqldelight.sqlite)
    implementation(libs.sqldelight.coroutines)

    // 序列化（TemplateRepositoryImpl 需要）
    implementation(libs.kotlinx.serialization.json)

    // Koin（用于 RepositoryModule）
    implementation(libs.koin.core)

    // 测试依赖
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)

    // 数据库测试工具（runDatabaseTest）
    testImplementation(projects.datastore.databaseTest)
}

