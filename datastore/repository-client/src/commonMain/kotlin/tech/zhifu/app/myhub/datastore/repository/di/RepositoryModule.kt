package tech.zhifu.app.myhub.datastore.repository.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.di.remoteDataSourceModule
import tech.zhifu.app.myhub.datastore.repository.auth.di.authModule
import tech.zhifu.app.myhub.datastore.repository.capture.di.captureRepositoryModule
import tech.zhifu.app.myhub.datastore.repository.card.di.repositoryCardModule
import tech.zhifu.app.myhub.datastore.repository.collection.di.collectionRepositoryModule
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage
import tech.zhifu.app.myhub.datastore.repository.store.DatabaseBookkeeperStorage
import tech.zhifu.app.myhub.datastore.repository.sync.di.syncRepositoryModule
import tech.zhifu.app.myhub.datastore.repository.tag.di.tagRepositoryModule
import tech.zhifu.app.myhub.datastore.repository.template.di.templateRepositoryModule
import tech.zhifu.app.myhub.datastore.repository.user.di.userRepositoryModule

/**
 * 仓库依赖注入模块（客户端）
 *
 * 提供所有 Repository 的实现
 * 包含本地和远程数据源模块（localDataSourceModule, remoteDataSourceModule）
 */
val repositoryModule = module {
    // Bookkeeper 存储（数据库实现 - 持久化）
    single<BookkeeperStorage> {
        DatabaseBookkeeperStorage(
            database = get<MyHubDatabase>()
        )
    }

    includes(
        databaseModule,
        localDataSourceModule,
        remoteDataSourceModule,
        authModule,
        captureRepositoryModule,
        repositoryCardModule(),
        tagRepositoryModule(),
        collectionRepositoryModule(),
        userRepositoryModule(),
        templateRepositoryModule(),
        syncRepositoryModule(),
    )
}
