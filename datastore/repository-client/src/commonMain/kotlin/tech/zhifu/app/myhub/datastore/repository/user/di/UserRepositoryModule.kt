package tech.zhifu.app.myhub.datastore.repository.user.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.store.createMutableStore
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreCache
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreDataValidator
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreUpdater
import tech.zhifu.app.myhub.logger.logger

fun userRepositoryModule() = module {
    single<UserRepository> {
        val logger = logger("user-repo")
        UserRepositoryImpl(
            store = createMutableStore(
                memoryCache = createUserStoreCache(),
                validator = createUserStoreDataValidator(),
                sourceOfTruth = createUserStoreSourceOfTruth(
                    localUserDataSource = get(),
                ),
                fetcher = createUserStoreFetcher(
                    remoteUserDataSource = get()
                ),
                updater = createUserStoreUpdater(
                    remoteUserDataSource = get(),
                    logger = logger,
                ),
                bookkeeper = createUserStoreBookkeeper(
                    bookkeeperStorage = get()
                ),
            )
        )
    }
}
