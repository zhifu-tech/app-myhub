package tech.zhifu.app.myhub.datastore.datasource.di

import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.RemoteAuthDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCollectionDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteAuthDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteCardTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteCollectionDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteSyncDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.TokenRefreshProviderAdapter
import tech.zhifu.app.myhub.network.auth.TokenRefreshProvider
import tech.zhifu.app.myhub.network.createHttpClient
import tech.zhifu.app.myhub.network.di.networkModule

/**
 * 远程数据源依赖注入模块
 *
 * 提供所有 RemoteDataSource 的实现
 * 包含 networkModule（提供 HttpClient 和 KtorClientFactory）
 */
val remoteDataSourceModule = module {
    // 包含网络模块（提供基础组件，但不包含带认证的 HttpClient）
    includes(networkModule)

    // 创建带认证的 HttpClient（使用 TokenStorage 和 RemoteAuthDataSource）
    // 注意：这里需要先创建 RemoteAuthDataSource，但 RemoteAuthDataSource 需要 HttpClient
    // 解决方案：先创建一个临时的 HttpClient 用于 RemoteAuthDataSource
    // 然后创建带认证的 HttpClient，RemoteAuthDataSource 在 refreshTokens 回调中使用
    // 由于刷新 API 不需要认证（由 sendWithoutRequest 控制），可以使用同一个 HttpClient

    // 步骤 1：创建临时的 HttpClient 用于 RemoteAuthDataSource
    single<HttpClient>(named("temp")) {
        createHttpClient()
    }

    // 步骤 2：创建 RemoteAuthDataSource（使用临时 HttpClient）
    single<RemoteAuthDataSource> {
        RemoteAuthDataSourceImpl(
            httpClient = get(named("temp"))
        )
    }

    // 步骤 3：创建 TokenRefreshProvider 适配器
    single<TokenRefreshProvider> {
        TokenRefreshProviderAdapter(
            remoteAuthDataSource = get<RemoteAuthDataSource>()
        )
    }

    // 步骤 4：创建带认证的 HttpClient（用于所有业务 API）
    // 这个 HttpClient 的 refreshTokens 回调会使用 TokenRefreshProvider
    single<HttpClient> {
        createHttpClient(
            tokenStorage = get(),
            tokenRefreshProvider = get()
        )
    }

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

    single<RemoteUserDataSource> {
        RemoteUserDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }

    single<RemoteCollectionDataSource> {
        RemoteCollectionDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }

    single<RemoteCardTemplateDataSource> {
        RemoteCardTemplateDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }

    single<RemoteSyncDataSource> {
        RemoteSyncDataSourceImpl(
            httpClient = get<HttpClient>()
        )
    }
}

