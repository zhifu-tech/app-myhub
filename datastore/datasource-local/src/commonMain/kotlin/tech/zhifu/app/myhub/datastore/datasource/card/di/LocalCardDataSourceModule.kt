package tech.zhifu.app.myhub.datastore.datasource.card.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.card.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.card.LocalCardDataSourceImpl

val localCardDataSourceModule = module {
    single<LocalCardDataSource> {
        LocalCardDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
}
