package tech.zhifu.app.myhub.datastore.datasource.sync.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.sync.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.sync.LocalSyncDataSourceImpl

val localSyncDataSourceModule = module {
    single<LocalSyncDataSource> {
        LocalSyncDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
}
