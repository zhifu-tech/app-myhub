plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(projects.core.datastoreDatabase)
    implementation(projects.core.datastoreDatabaseManage)
    implementation(libs.sqldelight.sqlite)
    implementation(libs.postgresql)
    implementation(libs.koin.core)
}

