package tech.zhifu.app.myhub.datastore.repository.capture.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepositoryImpl

val captureRepositoryModule = module {
    single<CaptureLocalRepository> {
        CaptureLocalRepositoryImpl(
            database = get<MyHubDatabase>(),
        )
    }
}
