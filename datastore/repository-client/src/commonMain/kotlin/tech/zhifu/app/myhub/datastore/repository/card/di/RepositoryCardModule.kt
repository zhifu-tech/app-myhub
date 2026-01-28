package tech.zhifu.app.myhub.datastore.repository.card.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.card.CardRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.card.CardSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreCache
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.store.createMutableStore
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun repositoryCardModule() = module {
    single<CardRepository> {
        val logger = logger("card-repo")
        CardRepositoryImpl(
            store = createMutableStore(
                cache = createCardStoreCache(),
                sourceOfTruth = createCardStoreSourceOfTruth(
                    localCardDataSource = get(),
                    logger = logger,
                ),
                bookkeeper = createCardStoreBookkeeper(
                    bookkeeperStorage = get()
                ),
                fetcher = createCardStoreFetcher(
                    remoteCardDataSource = get()
                ),
                updater = createCardStoreUpdater(
                    remoteCardDataSource = get(),
                    logger = logger,
                ),
            ),
            syncRepository = get(),
            tagRepository = get(),
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
