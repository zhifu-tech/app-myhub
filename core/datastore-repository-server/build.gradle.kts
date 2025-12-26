plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(projects.core.platform)

    // Repository 接口
    api(projects.core.datastoreRepository)

    implementation(projects.core.datastoreDatabase)
    // 数据库服务端模块（MyHubDatabase）
    implementation(projects.core.datastoreDatabaseServer)

    // LocalDataSource（复用客户端实现，避免代码重复）
    implementation(projects.core.datastoreDatasourceLocal)

    // Model（包含 DTO，用于 Service 层转换）
    implementation(projects.core.datastoreModel)

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
    testImplementation(projects.core.datastoreDatabaseTest)
}

