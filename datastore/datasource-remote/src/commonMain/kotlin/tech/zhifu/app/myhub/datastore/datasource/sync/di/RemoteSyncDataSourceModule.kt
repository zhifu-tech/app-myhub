package tech.zhifu.app.myhub.datastore.datasource.sync.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.datastore.datasource.sync.RemoteSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.sync.RemoteSyncDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.sync.RemoteSyncDataSourceNoop

val remoteSyncDataSourceModule = module {
    single<RemoteSyncDataSource> {
        if (AppBuildConfig.enableServer) {
            RemoteSyncDataSourceImpl(
                httpClient = get()
            )
        } else {
            RemoteSyncDataSourceNoop()
        }
    }
}
