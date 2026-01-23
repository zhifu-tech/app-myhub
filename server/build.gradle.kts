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
    implementation(projects.core.logger)

    implementation(projects.datastore.model)
    implementation(projects.datastore.sync)
    implementation(projects.datastore.repositoryServer)
    implementation(projects.datastore.databaseServer)
    implementation(projects.datastore.datasourceLocal)

    implementation(libs.ktor.http)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.utils)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.logback)

    implementation(libs.koin.core)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
