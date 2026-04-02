package tech.zhifu.app.myhub.datastore.repository.user.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.store.createMutableStore
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.user.UserSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreCache
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreUpdater
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

fun userRepositoryModule() = module {
    single<UserRepository> {
        val logger = logger("user-repo")
        UserRepositoryImpl(
            logger = logger,
            syncRepository = get(),
            store = createMutableStore(
                cache = createUserStoreCache(),
                sourceOfTruth = createUserStoreSourceOfTruth(
                    localUserDataSource = get(),
                ),
                bookkeeper = createUserStoreBookkeeper(
                    bookkeeperStorage = get()
                ),
                fetcher = createUserStoreFetcher(
                    remoteUserDataSource = get()
                ),
                updater = createUserStoreUpdater(
                    remoteUserDataSource = get(),
                    logger = logger,
                ),
            )
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.User.value)
    ) {
        UserSyncChangeApplier(
            userRepo = get(),
            syncRepo = get(),
        )
    }
}
