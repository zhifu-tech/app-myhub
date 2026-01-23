plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.core.logger)
    implementation(projects.datastore.database)
    implementation(projects.datastore.datasourceLocal)
    implementation(projects.datastore.databaseServer)
    implementation(projects.datastore.repositoryServer)
    implementation(projects.datastore.sync)
    implementation(projects.datastore.model)

    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.sqlite)
    implementation(libs.sqldelight.coroutines)

    testImplementation(projects.datastore.databaseTest)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

