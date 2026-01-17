plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.core.logger)

    api(projects.datastore.repository)

    implementation(projects.datastore.database)
    implementation(projects.datastore.databaseServer)

    implementation(projects.datastore.datasourceLocal)

    implementation(projects.datastore.model)

    implementation(libs.sqldelight.sqlite)
    implementation(libs.sqldelight.coroutines)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(projects.datastore.databaseTest)
}

