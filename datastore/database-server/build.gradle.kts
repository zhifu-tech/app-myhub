plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.datastore.database)
    implementation(projects.datastore.databaseManage)
    implementation(libs.sqldelight.sqlite)
    implementation(libs.postgresql)
    implementation(libs.koin.core)
}

