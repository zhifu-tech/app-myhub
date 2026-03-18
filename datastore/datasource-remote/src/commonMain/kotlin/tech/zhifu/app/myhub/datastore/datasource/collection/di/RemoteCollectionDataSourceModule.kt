package tech.zhifu.app.myhub.datastore.datasource.collection.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.datastore.datasource.collection.RemoteCollectionDataSource
import tech.zhifu.app.myhub.datastore.datasource.collection.RemoteCollectionDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.collection.RemoteCollectionDataSourceNoop

val remoteCollectionDataSourceModule = module {
    single<RemoteCollectionDataSource> {
        if (AppBuildConfig.enableServer) {
            RemoteCollectionDataSourceImpl(
                httpClient = get()
            )
        } else {
            RemoteCollectionDataSourceNoop()
        }
    }
}
