package tech.zhifu.app.myhub.datastore.network.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.network.KtorClientFactory
import tech.zhifu.app.myhub.datastore.network.createHttpClient

/**
 * 网络模块
 * 提供HttpClient和KtorClientFactory
 */
val networkModule = module {
    single<KtorClientFactory> {
        KtorClientFactory()
    }

    single<HttpClient> {
        createHttpClient(get())
    }
}

