package tech.zhifu.app.myhub.datastore.datasource.user.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.datastore.datasource.user.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.user.RemoteUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.user.RemoteUserDataSourceNoop

val remoteUserDataSourceModule = module {
    single<RemoteUserDataSource> {
        if (AppBuildConfig.enableServer) {
            RemoteUserDataSourceImpl(httpClient = get())
        } else {
            RemoteUserDataSourceNoop()
        }
    }
}
