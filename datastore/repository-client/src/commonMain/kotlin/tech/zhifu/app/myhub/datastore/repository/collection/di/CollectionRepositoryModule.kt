package tech.zhifu.app.myhub.datastore.repository.collection.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreCache
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.store.createMutableStore
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun collectionRepositoryModule() = module {
    single<CollectionRepository> {
        val logger = logger("collection-repo")
        CollectionRepositoryImpl(
            syncRepository = get(),
            store = createMutableStore(
                cache = createCollectionStoreCache(),
                sourceOfTruth = createCollectionStoreSourceOfTruth(
                    localCollectionDataSource = get(),
                    logger = logger,
                ),
                bookkeeper = createCollectionStoreBookkeeper(
                    bookkeeperStorage = get()
                ),
                fetcher = createCollectionStoreFetcher(
                    remoteCollectionDataSource = get()
                ),
                updater = createCollectionStoreUpdater(
                    remoteCollectionDataSource = get(),
                    logger = logger,
                ),
            ),
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.Collection.value)
    ) {
        CollectionSyncChangeApplier(
            collectionRepo = get(),
            syncRepo = get(),
        )
    }
}
