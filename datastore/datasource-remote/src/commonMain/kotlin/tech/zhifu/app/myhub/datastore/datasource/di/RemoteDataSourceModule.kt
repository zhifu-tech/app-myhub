package tech.zhifu.app.myhub.datastore.datasource.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.auth.di.remoteAuthDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.sync.di.remoteSyncDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.user.di.remoteUserDataSourceModule

val remoteDataSourceModule = module {
    includes(
        remoteAuthDataSourceModule,
        remoteSyncDataSourceModule,
        remoteUserDataSourceModule
    )
}
