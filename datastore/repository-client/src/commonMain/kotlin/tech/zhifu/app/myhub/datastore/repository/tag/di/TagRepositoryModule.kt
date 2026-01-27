package tech.zhifu.app.myhub.datastore.repository.tag.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.datastore.repository.store.StoreFactory
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepository
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.tag.TagStore
import tech.zhifu.app.myhub.datastore.repository.tag.TagStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.tag.TagStoreCache
import tech.zhifu.app.myhub.datastore.repository.tag.TagStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.tag.TagStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.tag.TagStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.tag.TagSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreCache
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreUpdater
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun tagRepositoryModule() = module {
    factory<TagStoreCache> {
        createTagStoreCache()
    }
    factory<TagStoreSourceOfTruth> {
        createTagStoreSourceOfTruth(
            localTagDataSource = get()
        )
    }
    factory<TagStoreBookkeeper> {
        createTagStoreBookkeeper(
            bookkeeperStorage = get()
        )
    }
    factory<TagStoreFetcher> {
        createTagStoreFetcher(
            remoteTagDataSource = get<RemoteTagDataSource>()
        )
    }
    factory<TagStoreUpdater> {
        createTagStoreUpdater(
            remoteTagDataSource = get()
        )
    }
    factory<TagStore> {
        StoreFactory.createMutableStore(
            cache = get(),
            sourceOfTruth = get(),
            bookkeeper = get(),
            fetcher = get(),
            updater = get(),
        )
    }
    single<TagRepository> {
        TagRepositoryImpl(
            store = get<TagStore>(),
            syncRepository = get<SyncRepository>(),
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.Tag.value)
    ) {
        TagSyncChangeApplier(
            tagRepo = get<TagRepository>(),
            syncRepo = get<SyncRepository>(),
        )
    }
}
