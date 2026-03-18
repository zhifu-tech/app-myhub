package tech.zhifu.app.myhub.datastore.datasource.user.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.user.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.user.LocalUserDataSourceImpl

val localUserDataSourceModule = module {
    single<LocalUserDataSource> {
        LocalUserDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
}
