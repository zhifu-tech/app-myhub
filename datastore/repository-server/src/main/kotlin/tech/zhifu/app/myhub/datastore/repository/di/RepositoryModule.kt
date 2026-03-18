package tech.zhifu.app.myhub.datastore.repository.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.datasource.card.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.card.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.collection.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.datasource.sync.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.tag.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.user.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule
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
    single<TagRepository> {
        TagRepositoryImpl(
            localDataSource = get<LocalTagDataSource>()
        )
    }

    single<CardRepository> {
        CardRepositoryImpl(
            localDataSource = get<LocalCardDataSource>(),
            tagRepository = get<TagRepository>()
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

    single<CollectionRepository> {
        CollectionRepositoryImpl(
            localDataSource = get<LocalCollectionDataSource>()
        )
    }

    single<CardTemplateRepository> {
        CardTemplateRepositoryImpl(
            localDataSource = get<LocalCardTemplateDataSource>()
        )
    }
}

