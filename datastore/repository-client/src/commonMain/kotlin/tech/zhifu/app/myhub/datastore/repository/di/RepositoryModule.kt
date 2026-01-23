package tech.zhifu.app.myhub.datastore.repository.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.di.remoteDataSourceModule
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.CardTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.datastore.repository.impl.CardRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.CardTemplateRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.CollectionRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.SyncRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.TagRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.UserRepositoryImpl

/**
 * 仓库依赖注入模块（客户端）
 *
 * 提供所有 Repository 的实现
 * 包含本地和远程数据源模块（localDataSourceModule, remoteDataSourceModule）
 */
val repositoryModule = module {
    // 包含数据源模块（提供 LocalDataSource 和 RemoteDataSource）
    includes(
        databaseModule,
        localDataSourceModule,
        remoteDataSourceModule
    )

    single<UserRepository> {
        UserRepositoryImpl(
            localUserDataSource = get<LocalUserDataSource>(),
            remoteUserDataSource = get<RemoteUserDataSource>(),
            localSyncDataSource = get<LocalSyncDataSource>(),
        )
    }

    single<TagRepository> {
        TagRepositoryImpl(
            localTagDataSource = get<LocalTagDataSource>(),
            localSyncDataSource = get<LocalSyncDataSource>(),
        )
    }

    single<CollectionRepository> {
        CollectionRepositoryImpl(
            localCollectionDataSource = get<LocalCollectionDataSource>(),
            localSyncDataSource = get<LocalSyncDataSource>(),
        )
    }

    single<CardRepository> {
        CardRepositoryImpl(
            localCardDataSource = get<LocalCardDataSource>(),
            remoteCardDataSource = get<RemoteCardDataSource>(),
            localSyncDataSource = get<LocalSyncDataSource>(),
            tagRepository = get<TagRepository>()
        )
    }

    single<CardTemplateRepository> {
        CardTemplateRepositoryImpl(
            localCardTemplateDataSource = get<LocalCardTemplateDataSource>(),
            localSyncDataSource = get<LocalSyncDataSource>()
        )
    }

    single<SyncRepository> {
        SyncRepositoryImpl(
            localSyncDataSource = get<LocalSyncDataSource>(),
            remoteSyncDataSource = get<RemoteSyncDataSource>(),
            cardRepository = get<CardRepository>(),
            userRepository = get<UserRepository>(),
            tagRepository = get<TagRepository>(),
            collectionRepository = get<CollectionRepository>(),
            cardTemplateRepository = get<CardTemplateRepository>()
        )
    }
}

