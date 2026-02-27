package tech.zhifu.app.myhub.datastore.repository.capture.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureRepository
import tech.zhifu.app.myhub.datastore.repository.capture.HttpCaptureRepository

val captureRepositoryModule = module {
    single<CaptureRepository> {
        HttpCaptureRepository()
    }
}
