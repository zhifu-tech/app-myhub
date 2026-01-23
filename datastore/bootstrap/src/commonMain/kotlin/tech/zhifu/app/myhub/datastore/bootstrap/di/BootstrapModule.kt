package tech.zhifu.app.myhub.datastore.bootstrap.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.CardTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.datastore.repository.UserRepository

val bootstrapModule = module {
    factory {
        Bootstrap(
            userRepository = get<UserRepository>(),
            tagRepository = get<TagRepository>(),
            collectionRepository = get<CollectionRepository>(),
            cardRepository = get<CardRepository>(),
            cardTemplateRepository = get<CardTemplateRepository>(),
        )
    }
}
