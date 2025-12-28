plugins {
    alias(libs.plugins.myhub.kmp)
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
            implementation(projects.core.logger)

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

