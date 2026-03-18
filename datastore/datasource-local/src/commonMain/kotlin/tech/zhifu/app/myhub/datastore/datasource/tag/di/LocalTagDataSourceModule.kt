package tech.zhifu.app.myhub.datastore.datasource.tag.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.tag.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.tag.LocalTagDataSourceImpl

val localTagDataSourceModule = module {
    single<LocalTagDataSource> {
        LocalTagDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
}
