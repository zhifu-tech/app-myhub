package tech.zhifu.app.myhub.datastore.repository.collection.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionStore
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionStoreCache
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreCache
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.collection.createCollectionStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.store.StoreFactory
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun collectionRepositoryModule() = module {
    factory<CollectionStoreCache> {
        createCollectionStoreCache()
    }
    factory<CollectionStoreSourceOfTruth> {
        createCollectionStoreSourceOfTruth(
            localCollectionDataSource = get()
        )
    }
    factory<CollectionStoreBookkeeper> {
        createCollectionStoreBookkeeper(
            bookkeeperStorage = get()
        )
    }
    factory<CollectionStoreFetcher> {
        createCollectionStoreFetcher(
            localCollectionDataSource = get()
        )
    }
    factory<CollectionStoreUpdater> {
        createCollectionStoreUpdater()
    }
    factory<CollectionStore> {
        StoreFactory.createMutableStore(
            cache = get(),
            sourceOfTruth = get(),
            bookkeeper = get(),
            fetcher = get(),
            updater = get(),
        )
    }
    single<CollectionRepository> {
        CollectionRepositoryImpl(
            store = get<CollectionStore>(),
            syncRepository = get<SyncRepository>(),
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.Collection.value)
    ) {
        CollectionSyncChangeApplier(
            collectionRepo = get<CollectionRepository>(),
            syncRepo = get<SyncRepository>(),
        )
    }
}
