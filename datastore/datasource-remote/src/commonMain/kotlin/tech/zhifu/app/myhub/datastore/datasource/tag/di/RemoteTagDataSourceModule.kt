package tech.zhifu.app.myhub.datastore.datasource.tag.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.datastore.datasource.tag.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.tag.RemoteTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.tag.RemoteTagDataSourceNoop

val remoteTagDataSourceModule = module {
    single<RemoteTagDataSource> {
        if (AppBuildConfig.enableServer) {
            RemoteTagDataSourceImpl(
                httpClient = get()
            )
        } else {
            RemoteTagDataSourceNoop()
        }
    }
}
