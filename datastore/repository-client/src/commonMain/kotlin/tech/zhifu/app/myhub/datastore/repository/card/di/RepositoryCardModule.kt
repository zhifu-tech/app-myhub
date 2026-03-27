package tech.zhifu.app.myhub.datastore.repository.card.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.card.CardRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreCache
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.card.createCardStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.store.createMutableStore

fun repositoryCardModule() = module {
    single<CardRepository> {
        CardRepositoryImpl(
            store = createMutableStore(
                cache = createCardStoreCache(),
                sourceOfTruth = createCardStoreSourceOfTruth(
                    localCardDataSource = get(),
                ),
                bookkeeper = createCardStoreBookkeeper(
                    bookkeeperStorage = get()
                ),
                fetcher = createCardStoreFetcher(),
                updater = createCardStoreUpdater(),
            ),
        )
    }
}
