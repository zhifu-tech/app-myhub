package tech.zhifu.app.myhub.datastore.datasource.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCollectionDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalSyncDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalUserDataSourceImpl

val localDataSourceModule = module {

    single<LocalUserDataSource> {
        LocalUserDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }

    single<LocalCardDataSource> {
        LocalCardDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }

    single<LocalCollectionDataSource> {
        LocalCollectionDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }

    single<LocalTagDataSource> {
        LocalTagDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }

    single<LocalSyncDataSource> {
        LocalSyncDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }

    single<LocalCardTemplateDataSource> {
        LocalCardTemplateDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
}

