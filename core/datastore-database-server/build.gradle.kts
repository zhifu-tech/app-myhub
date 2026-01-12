plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.core.datastoreDatabase)
    implementation(projects.core.datastoreDatabaseManage)
    implementation(libs.sqldelight.sqlite)
    implementation(libs.postgresql)
    implementation(libs.koin.core)
}

