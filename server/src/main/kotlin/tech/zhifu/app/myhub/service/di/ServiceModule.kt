package tech.zhifu.app.myhub.service.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.service.CardService
import tech.zhifu.app.myhub.service.SyncService
import tech.zhifu.app.myhub.service.UserService

val serviceModule = module {
    factory<CardService> {
        CardService(
            cardRepository = get<CardRepository>()
        )
    }

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
}

