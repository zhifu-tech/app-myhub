package tech.zhifu.app.myhub.datastore.repository.user.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.repository.store.StoreFactory
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.user.UserPreferencesSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.user.UserStore
import tech.zhifu.app.myhub.datastore.repository.user.UserStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.user.UserStoreCache
import tech.zhifu.app.myhub.datastore.repository.user.UserStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.user.UserStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.user.UserStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.user.UserSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreCache
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.user.createUserStoreUpdater
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun userRepositoryModule() = module {
    factory<UserStoreCache> {
        createUserStoreCache()
    }
    factory<UserStoreSourceOfTruth> {
        createUserStoreSourceOfTruth(
            localUserDataSource = get()
        )
    }
    factory<UserStoreBookkeeper> {
        createUserStoreBookkeeper(
            bookkeeperStorage = get()
        )
    }
    factory<UserStoreFetcher> {
        createUserStoreFetcher(
            remoteUserDataSource = get()
        )
    }
    factory<UserStoreUpdater> {
        createUserStoreUpdater(
            remoteUserDataSource = get()
        )
    }
    factory<UserStore> {
        StoreFactory.createMutableStore(
            cache = get(),
            sourceOfTruth = get(),
            bookkeeper = get(),
            fetcher = get(),
            updater = get(),
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            store = get(),
            localUserDataSource = get(),
            syncRepository = get(),
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
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.UserPreferences.value)
    ) {
        UserPreferencesSyncChangeApplier(
            userRepo = get(),
            syncRepo = get(),
        )
    }
}
