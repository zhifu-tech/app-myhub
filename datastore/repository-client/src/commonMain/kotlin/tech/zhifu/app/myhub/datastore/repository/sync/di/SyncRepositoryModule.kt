package tech.zhifu.app.myhub.datastore.repository.sync.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteSyncDataSource
import tech.zhifu.app.myhub.datastore.repository.sync.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.sync.SyncCoordinatorImpl
import tech.zhifu.app.myhub.datastore.repository.sync.SyncForegroundScheduler
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.sync.SyncStatusWrapper
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.sync.SyncCoordinator
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncScheduler

fun syncRepositoryModule() = module {
    factory<Map<SyncEntityType, SyncChangeApplier?>> {
        SyncEntityType.entries.associateWith { entityType ->
            getOrNull<SyncChangeApplier>(qualifier = named(entityType.value))
        }
    }
//    factory<CoroutineScope>(qualifier = named<SyncScheduler>()) {
//        CoroutineScope(SupervisorJob() + Dispatchers.Default)
//    }
    single<SyncStatusWrapper> {
        SyncStatusWrapper()
    }
    factory<SyncCoordinator> {
        SyncCoordinatorImpl(
            localSyncDataSource = get<LocalSyncDataSource>(),
            remoteSyncDataSource = get<RemoteSyncDataSource>(),
            syncAppliers = lazy { get<Map<SyncEntityType, SyncChangeApplier?>>() },
            status = get(),
        )
    }
    factory<SyncScheduler> {
        SyncForegroundScheduler(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            coordinator = lazy { get<SyncCoordinator>() }
        )
    }
    single<SyncRepository> {
        SyncRepositoryImpl(
            localSyncDataSource = get<LocalSyncDataSource>(),
            userRepository = lazy { get<UserRepository>() },
            syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            syncScheduler = get<SyncScheduler>(),
            syncStatus = get(),
            syncCoordinator = lazy { get<SyncCoordinator>() }
        )
    }
}
