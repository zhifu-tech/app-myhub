package tech.zhifu.app.myhub.datastore.datasource.collection.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.collection.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.datasource.collection.LocalCollectionDataSourceImpl

val localCollectionDataSourceModule = module {
    single<LocalCollectionDataSource> {
        LocalCollectionDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
}
