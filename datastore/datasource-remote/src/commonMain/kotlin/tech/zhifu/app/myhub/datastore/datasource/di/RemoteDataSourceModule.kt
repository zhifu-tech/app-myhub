package tech.zhifu.app.myhub.datastore.datasource.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.auth.di.remoteAuthDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.card.di.remoteCardDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.collection.di.remoteCollectionDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.sync.di.remoteSyncDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.tag.di.remoteTagDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.user.di.remoteUserDataSourceModule

/**
 * 远程数据源依赖注入模块
 *
 * 聚合各 domain 的 RemoteDataSource 模块（auth/card/tag/collection/sync/user）。
 * 具体的 HttpClient / networkModule 由 auth 模块内部提供。
 */
val remoteDataSourceModule = module {
    includes(
        remoteAuthDataSourceModule,
        remoteCardDataSourceModule,
        remoteCollectionDataSourceModule,
        remoteSyncDataSourceModule,
        remoteTagDataSourceModule,
        remoteUserDataSourceModule
    )
}
