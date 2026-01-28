package tech.zhifu.app.myhub.datastore.repository.template.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import tech.zhifu.app.myhub.datastore.repository.store.createMutableStore
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.template.CardTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.template.CardTemplateRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.template.TemplateSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreCache
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.template.createTemplateStoreUpdater
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
fun templateRepositoryModule() = module {
    single<CardTemplateRepository> {
        val logger = logger("template-repo")
        CardTemplateRepositoryImpl(
            syncRepository = get(),
            store = createMutableStore(
                cache = createTemplateStoreCache(),
                sourceOfTruth = createTemplateStoreSourceOfTruth(
                    localCardTemplateDataSource = get(),
                    logger = logger,
                ),
                bookkeeper = createTemplateStoreBookkeeper(
                    bookkeeperStorage = get()
                ),
                fetcher = createTemplateStoreFetcher(
                    remoteCardTemplateDataSource = get()
                ),
                updater = createTemplateStoreUpdater(
                    remoteCardTemplateDataSource = get(),
                    logger = logger,
                ),
            ),
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.Template.value)
    ) {
        TemplateSyncChangeApplier(
            templateRepo = get(),
            syncRepo = get(),
        )
    }
}
