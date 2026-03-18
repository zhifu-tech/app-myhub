package tech.zhifu.app.myhub.datastore.datasource.card.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.datastore.datasource.card.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.card.RemoteCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.card.RemoteCardDataSourceNoop

val remoteCardDataSourceModule = module {
    single<RemoteCardDataSource> {
        if (AppBuildConfig.enableServer) {
            RemoteCardDataSourceImpl(
                httpClient = get()
            )
        } else {
            RemoteCardDataSourceNoop()
        }
    }
}
