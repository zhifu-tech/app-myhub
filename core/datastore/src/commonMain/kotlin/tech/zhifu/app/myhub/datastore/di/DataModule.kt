package tech.zhifu.app.myhub.datastore.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalStatisticsDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalStatisticsDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteCardDataSourceImpl
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
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * 数据层依赖注入模块
 * 
 * 提供数据源和仓库的实现
 */
val dataModule = module {
    // LocalDataSource 实现（使用SQLDelight）
    single<LocalCardDataSource> {
        LocalCardDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalTagDataSource> {
        LocalTagDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalTemplateDataSource> {
        LocalTemplateDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalUserDataSource> {
        LocalUserDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalStatisticsDataSource> {
        LocalStatisticsDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    // RemoteDataSource 实现（使用Ktor Client）
    single<RemoteCardDataSource> {
        RemoteCardDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }
    
    // Repository 绑定
    single<CardRepository> {
        CardRepositoryImpl(
            localDataSource = get<LocalCardDataSource>(),
            remoteDataSource = get<RemoteCardDataSource>()
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
        UserRepositoryImpl(
            localDataSource = get<LocalUserDataSource>()
        )
    }
    
    single<StatisticsRepository> {
        StatisticsRepositoryImpl(
            localDataSource = get<LocalStatisticsDataSource>()
        )
    }
}

