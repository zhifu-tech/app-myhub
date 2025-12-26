package tech.zhifu.app.myhub.datastore.repository.di

import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule
import tech.zhifu.app.myhub.datastore.repository.CardRepository
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
            localDataSource = get<LocalCardDataSource>()
        )
    }

    single<TagRepository> {
        TagRepositoryImpl(
            localDataSource = get<LocalTagDataSource>()
        )
    }

    single<TemplateRepository> {
        TemplateRepositoryImpl(
            localDataSource = get<LocalTemplateDataSource>()
        )
    }

    single<UserRepository> {
        val repository = UserRepositoryImpl(
            localDataSource = get<LocalUserDataSource>()
        )
        // 初始化默认用户（如果不存在）
        runBlocking {
            repository.initializeDefaultUserIfNeeded()
        }
        repository
    }

    single<StatisticsRepository> {
        StatisticsRepositoryImpl(
            cardDataSource = get<LocalCardDataSource>()
        )
    }
}

