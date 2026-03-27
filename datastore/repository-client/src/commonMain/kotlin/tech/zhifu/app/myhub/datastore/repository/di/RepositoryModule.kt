package tech.zhifu.app.myhub.datastore.repository.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.datasource.di.localDataSourceModule
import tech.zhifu.app.myhub.datastore.datasource.di.remoteDataSourceModule
import tech.zhifu.app.myhub.datastore.repository.auth.di.authModule
import tech.zhifu.app.myhub.datastore.repository.capture.di.captureRepositoryModule
import tech.zhifu.app.myhub.datastore.repository.card.di.repositoryCardModule
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage
import tech.zhifu.app.myhub.datastore.repository.store.DatabaseBookkeeperStorage
import tech.zhifu.app.myhub.datastore.repository.sync.di.syncRepositoryModule
import tech.zhifu.app.myhub.datastore.repository.user.di.userRepositoryModule

fun repositoryModule() = module {
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
        userRepositoryModule(),
        syncRepositoryModule(),
    )
}
