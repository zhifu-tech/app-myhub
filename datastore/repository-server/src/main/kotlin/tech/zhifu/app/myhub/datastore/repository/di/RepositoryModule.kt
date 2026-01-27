package tech.zhifu.app.myhub.datastore.repository.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.impl.CardRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.SyncRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.UserRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

/**
 * 仓库依赖注入模块（服务端）
 *
 * 提供所有 Repository 的实现（服务端版本）
 * 所有 Repository 都使用 LocalDataSource，保持架构一致性
 */
val repositoryModule = module {
    // 包含本地数据源模块
    includes(databaseModule)
    includes(localDataSourceModule)

    // Repository 实现（服务端）
    single<CardRepository> {
        CardRepositoryImpl(
            localDataSource = get<LocalCardDataSource>(),
        )
    }

    single<UserRepository> {
        val repository = UserRepositoryImpl(
            localDataSource = get<LocalUserDataSource>()
        )
        repository
    }

    single<SyncRepository> {
        SyncRepositoryImpl(
            localSyncDataSource = get<LocalSyncDataSource>()
        )
    }
}

