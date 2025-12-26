package tech.zhifu.app.myhub.datastore.repository.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalStatisticsDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteStatisticsDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.di.remoteDataSourceModule
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.ReactiveCardRepository
import tech.zhifu.app.myhub.datastore.repository.ReactiveStatisticsRepository
import tech.zhifu.app.myhub.datastore.repository.ReactiveTagRepository
import tech.zhifu.app.myhub.datastore.repository.ReactiveTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.ReactiveUserRepository
import tech.zhifu.app.myhub.datastore.repository.StatisticsRepository
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.datastore.repository.TemplateRepository
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.datastore.repository.impl.CardRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.StatisticsRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.TagRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.impl.TemplateRepositoryImpl
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

    // Repository 绑定（使用响应式接口）
    single<ReactiveCardRepository> {
        CardRepositoryImpl(
            localDataSource = get<LocalCardDataSource>(),
            remoteDataSource = get<RemoteCardDataSource>()
        )
    }

    // 同时绑定基础接口（用于兼容性）
    single<CardRepository> {
        get<ReactiveCardRepository>()
    }

    single<ReactiveTagRepository> {
        TagRepositoryImpl(
            localDataSource = get<LocalTagDataSource>(),
            remoteDataSource = get<RemoteTagDataSource>()
        )
    }

    // 同时绑定基础接口（用于兼容性）
    single<TagRepository> {
        get<ReactiveTagRepository>()
    }

    single<ReactiveTemplateRepository> {
        TemplateRepositoryImpl(
            localDataSource = get<LocalTemplateDataSource>(),
            remoteDataSource = get<RemoteTemplateDataSource>()
        )
    }

    // 同时绑定基础接口（用于兼容性）
    single<TemplateRepository> {
        get<ReactiveTemplateRepository>()
    }

    single<ReactiveUserRepository> {
        UserRepositoryImpl(
            localDataSource = get<LocalUserDataSource>(),
            remoteDataSource = get<RemoteUserDataSource>()
        )
    }

    // 同时绑定基础接口（用于兼容性）
    single<UserRepository> {
        get<ReactiveUserRepository>()
    }

    single<ReactiveStatisticsRepository> {
        StatisticsRepositoryImpl(
            localDataSource = get<LocalStatisticsDataSource>(),
            remoteDataSource = get<RemoteStatisticsDataSource>()
        )
    }

    // 同时绑定基础接口（用于兼容性）
    single<StatisticsRepository> {
        get<ReactiveStatisticsRepository>()
    }
}

