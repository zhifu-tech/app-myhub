package tech.zhifu.app.myhub.datastore.repository.template.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.repository.store.StoreFactory
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.template.CardTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.template.CardTemplateRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.template.TemplateStore
import tech.zhifu.app.myhub.datastore.repository.template.TemplateStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.template.TemplateStoreCache
import tech.zhifu.app.myhub.datastore.repository.template.TemplateStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.template.TemplateStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.template.TemplateStoreUpdater
import tech.zhifu.app.myhub.datastore.repository.template.TemplateSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreCache
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreUpdater
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun templateRepositoryModule() = module {
    factory<TemplateStoreCache> {
        createTemplateStoreCache()
    }
    factory<TemplateStoreSourceOfTruth> {
        createTemplateStoreSourceOfTruth(
            localCardTemplateDataSource = get()
        )
    }
    factory<TemplateStoreBookkeeper> {
        createTemplateStoreBookkeeper(
            bookkeeperStorage = get()
        )
    }
    factory<TemplateStoreFetcher> {
        createTemplateStoreFetcher(
            localCardTemplateDataSource = get()
        )
    }
    factory<TemplateStoreUpdater> {
        createTemplateStoreUpdater()
    }
    factory<TemplateStore> {
        StoreFactory.createMutableStore(
            cache = get(),
            sourceOfTruth = get(),
            bookkeeper = get(),
            fetcher = get(),
            updater = get(),
        )
    }
    single<CardTemplateRepository> {
        CardTemplateRepositoryImpl(
            store = get<TemplateStore>(),
            syncRepository = get<SyncRepository>(),
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.Template.value)
    ) {
        TemplateSyncChangeApplier(
            templateRepo = get<CardTemplateRepository>(),
            syncRepo = get<SyncRepository>(),
        )
    }
}
