package tech.zhifu.app.myhub.datastore.datasource.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteStatisticsDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteStatisticsDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.network.di.networkModule

/**
 * 远程数据源依赖注入模块
 *
 * 提供所有 RemoteDataSource 的实现
 * 包含 networkModule（提供 HttpClient 和 KtorClientFactory）
 */
val remoteDataSourceModule = module {
    // 包含网络模块（提供 HttpClient 和 KtorClientFactory）
    includes(networkModule)

    // RemoteDataSource 实现（使用Ktor Client）
    single<RemoteCardDataSource> {
        RemoteCardDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }

    single<RemoteTagDataSource> {
        RemoteTagDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }

    single<RemoteTemplateDataSource> {
        RemoteTemplateDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }

    single<RemoteUserDataSource> {
        RemoteUserDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }

    single<RemoteStatisticsDataSource> {
        RemoteStatisticsDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }
}

