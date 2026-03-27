package tech.zhifu.app.myhub.service.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.auth.TokenService
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.service.SyncService
import tech.zhifu.app.myhub.service.UserService
import tech.zhifu.app.myhub.service.media.CaptureAnalysisService
import tech.zhifu.app.myhub.service.media.MediaUploadService
import tech.zhifu.app.myhub.service.media.analysis.AnalysisProvider
import tech.zhifu.app.myhub.service.media.analysis.AnalysisProviderConfig
import tech.zhifu.app.myhub.service.media.analysis.AnalysisProviderFactory

val serviceModule = module {

    factory<UserService> {
        UserService(
            userRepository = get<UserRepository>()
        )
    }

    factory<SyncService> {
        SyncService(
            syncRepository = get<SyncRepository>()
        )
    }

    factory<TokenService> {
        TokenService(
            userRepository = get<UserRepository>()
        )
    }

    single<MediaUploadService> {
        MediaUploadService()
    }

    single<AnalysisProviderConfig> {
        AnalysisProviderConfig.fromEnv()
    }

    single<AnalysisProviderFactory> {
        AnalysisProviderFactory(
            config = get<AnalysisProviderConfig>()
        )
    }

    single<AnalysisProvider> {
        get<AnalysisProviderFactory>().create()
    }

    single<CaptureAnalysisService> {
        CaptureAnalysisService(
            mediaUploadService = get<MediaUploadService>(),
            analysisProvider = get<AnalysisProvider>()
        )
    }
}
