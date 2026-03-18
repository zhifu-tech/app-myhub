package tech.zhifu.app.myhub.datastore.datasource.auth.di

import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.datastore.datasource.auth.RemoteAuthDataSource
import tech.zhifu.app.myhub.datastore.datasource.auth.RemoteAuthDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.auth.RemoteAuthDataSourceNoop
import tech.zhifu.app.myhub.datastore.datasource.auth.TokenRefreshProviderAdapter
import tech.zhifu.app.myhub.network.auth.TokenRefreshProvider
import tech.zhifu.app.myhub.network.createHttpClient
import tech.zhifu.app.myhub.network.di.networkModule

val remoteAuthDataSourceModule = module {
    includes(networkModule)

    single<HttpClient>(named("temp")) {
        createHttpClient()
    }

    single<RemoteAuthDataSource> {
        if (AppBuildConfig.enableServer) {
            RemoteAuthDataSourceImpl(
                httpClient = get(named("temp"))
            )
        } else {
            RemoteAuthDataSourceNoop()
        }
    }

    single<TokenRefreshProvider> {
        TokenRefreshProviderAdapter(
            remoteAuthDataSource = get()
        )
    }

    single<HttpClient> {
        if (AppBuildConfig.enableServer) {
            createHttpClient(
                tokenStorage = get(),
                tokenRefreshProvider = get()
            )
        } else {
            createHttpClient()
        }
    }
}
