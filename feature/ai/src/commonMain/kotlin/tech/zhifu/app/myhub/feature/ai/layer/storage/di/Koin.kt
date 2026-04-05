package tech.zhifu.app.myhub.feature.ai.layer.storage.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.layer.storage.job.AiJobRecoveryManager
import tech.zhifu.app.myhub.feature.ai.layer.storage.StorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.gateway.impl.RepositoryStorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaGarbageCollector
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaPostProcessExecutor

fun storageModule() = module {
    singleOf(::AiJobRecoveryManager)
    singleOf(::MediaGarbageCollector)
    singleOf(::MediaPostProcessExecutor)

    single<StorageGateway> {
        RepositoryStorageGateway(
            userRepository = get(),
            cardRepository = get(),
            captureLocalRepository = get(),
            mediaPostProcessExecutor = get(),
            mediaGarbageCollector = get(),
        )
    }
}
