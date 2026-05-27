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

    val isDevelopment: Boolean = project.hasProperty("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.core.logger)

    implementation(projects.datastore.model)
    implementation(projects.datastore.modelDto)
    implementation(projects.datastore.sync)
    implementation(projects.datastore.repositoryServerApi)
    implementation(projects.datastore.repositoryServer)
    implementation(projects.datastore.databaseServer)
    implementation(projects.datastore.datasourceLocal)

    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.http)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.ktor.utils)

    implementation(libs.logback)
    implementation(libs.jwt.auth0)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
