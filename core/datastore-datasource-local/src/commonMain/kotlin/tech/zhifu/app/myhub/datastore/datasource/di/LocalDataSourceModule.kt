package tech.zhifu.app.myhub.datastore.datasource.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalStatisticsDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalStatisticsDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * 本地数据源依赖注入模块
 * 
 * 提供所有 LocalDataSource 的实现
 */
val localDataSourceModule = module {
    // LocalDataSource 实现（使用SQLDelight）
    single<LocalCardDataSource> {
        LocalCardDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalTagDataSource> {
        LocalTagDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalTemplateDataSource> {
        LocalTemplateDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalUserDataSource> {
        LocalUserDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
    
    single<LocalStatisticsDataSource> {
        LocalStatisticsDataSourceImpl(
            database = get<MyHubDatabase>()
        )
    }
}

