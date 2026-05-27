plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.datastore.database)
    implementation(libs.sqldelight.sqlite)
    implementation(libs.postgresql)
    implementation(libs.koin.core)
}

