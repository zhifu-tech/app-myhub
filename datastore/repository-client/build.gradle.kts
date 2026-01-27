plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.myhub.kmp.web)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.repository.impl"
    }

    iosTargets().forEach { iosTarget ->
        iosTarget.binaries.all {
            linkerOpts += listOf("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.cache)
            implementation(projects.core.logger)
            implementation(projects.core.network)
            implementation(projects.datastore.database)
            implementation(projects.datastore.databaseClient)
            implementation(projects.datastore.datasourceLocal)
            implementation(projects.datastore.datasourceRemote)
            implementation(projects.datastore.model)
            implementation(projects.datastore.repositoryClientApi)
            implementation(projects.datastore.sync)

            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.mnf.store.cache5)
            implementation(libs.mnf.store.core5)
        }

        commonTest.dependencies {
            implementation(projects.datastore.databaseTest)

            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

