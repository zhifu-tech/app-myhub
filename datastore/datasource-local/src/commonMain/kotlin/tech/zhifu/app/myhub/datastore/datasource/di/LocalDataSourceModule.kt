package tech.zhifu.app.myhub.datastore.datasource.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.card.di.localCardDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.sync.di.localSyncDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.user.di.localUserDataSourceModule

val localDataSourceModule = module {
    includes(
        localUserDataSourceModule,
        localCardDataSourceModule,
        localSyncDataSourceModule
    )
}
