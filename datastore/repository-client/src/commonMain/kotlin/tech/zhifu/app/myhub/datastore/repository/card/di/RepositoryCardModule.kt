package tech.zhifu.app.myhub.datastore.repository.card.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.card.CardRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.card.CardStore
import tech.zhifu.app.myhub.datastore.repository.card.CardStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.card.CardStoreCache
import tech.zhifu.app.myhub.datastore.repository.card.CardStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.card.CardStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.card.CardStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.card.CardSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreCache
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.store.StoreFactory
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun repositoryCardModule() = module {
    factory<CardStoreCache> {
        createCardStoreCache()
    }
    factory<CardStoreSourceOfTruth> {
        createCardStoreSourceOfTruth(
            localCardDataSource = get()
        )
    }
    factory<CardStoreBookkeeper> {
        createCardStoreBookkeeper(
            bookkeeperStorage = get()
        )
    }
    factory<CardStoreFetcher> {
        createCardStoreFetcher(
            remoteCardDataSource = get()
        )
    }
    factory<CardStoreUpdater> {
        createCardStoreUpdater(
            remoteCardDataSource = get()
        )
    }
    factory<CardStore> {
        StoreFactory.createMutableStore(
            cache = get(),
            sourceOfTruth = get(),
            bookkeeper = get(),
            fetcher = get(),
            updater = get(),
        )
    }
    single<CardRepository> {
        CardRepositoryImpl(
            store = get(),
            syncRepository = get(),
            tagRepository = get()
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.Card.value)
    ) {
        CardSyncChangeApplier(
            cardRepo = get(),
            syncRepo = get(),
        )
    }
}
