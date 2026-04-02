package tech.zhifu.app.myhub.datastore.repository.capture.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepositoryImpl
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureRepository
import tech.zhifu.app.myhub.datastore.repository.capture.HttpCaptureRepository

val captureRepositoryModule = module {
    single<CaptureRepository> {
        HttpCaptureRepository()
    }
    single<CaptureLocalRepository> {
        CaptureLocalRepositoryImpl(
            database = get<MyHubDatabase>(),
        )
    }
}
