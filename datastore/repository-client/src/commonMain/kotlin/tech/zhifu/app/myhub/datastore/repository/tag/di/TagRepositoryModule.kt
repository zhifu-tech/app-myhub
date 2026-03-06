package tech.zhifu.app.myhub.datastore.repository.tag.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.store.createMutableStore
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepository
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.tag.TagSyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreBookkeeper
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreCache
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreFetcher
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreSourceOfTruth
import tech.zhifu.app.myhub.datastore.repository.tag.createTagStoreUpdater
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

fun tagRepositoryModule() = module {
    single<TagRepository> {
        val logger = logger("tag-repo")
        TagRepositoryImpl(
            syncRepository = get(),
            store = createMutableStore(
                cache = createTagStoreCache(),
                sourceOfTruth = createTagStoreSourceOfTruth(
                    localTagDataSource = get(),
                    logger = logger,
                ),
                bookkeeper = createTagStoreBookkeeper(
                    bookkeeperStorage = get(),
                    logger = logger,
                ),
                fetcher = createTagStoreFetcher(
                    remoteTagDataSource = get(),
                    logger = logger,
                ),
                updater = createTagStoreUpdater(
                    remoteTagDataSource = get(),
                    logger = logger,
                ),
            ),
        )
    }
    factory<SyncChangeApplier>(
        qualifier = named(SyncEntityType.Tag.value)
    ) {
        TagSyncChangeApplier(
            tagRepo = get(),
            syncRepo = get(),
        )
    }
}
