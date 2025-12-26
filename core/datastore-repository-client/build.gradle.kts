import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.datastore.repository.impl"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    js {
        browser()
    }


    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.all {
            linkerOpts += listOf("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.platform)

            // Repository 接口
            api(projects.core.datastoreRepository)

            // 数据源（LocalDataSource + RemoteDataSource）
            implementation(projects.core.datastoreDatabaseClient)
            implementation(projects.core.datastoreDatasourceLocal)
            implementation(projects.core.datastoreDatasourceRemote)

            // Model（包含 DTO）
            implementation(projects.core.datastoreModel)

            // Kotlinx Serialization（用于序列化支持，特别是枚举类）
            implementation(libs.kotlinx.serialization.json)

            // Kotlinx Coroutines（用于 Flow）
            implementation(libs.kotlinx.coroutines.core)

            // Koin（用于 RepositoryModule）
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

            // 数据库（用于测试）
            implementation(projects.core.datastoreDatabase)

            // 数据库测试工具（runDatabaseTest）
            implementation(projects.core.datastoreDatabaseTest)
        }
    }
}

