package tech.zhifu.app.myhub.datastore.bootstrap.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.bootstrap.startup.BootstrapStartupTask
import tech.zhifu.app.myhub.datastore.bootstrap.DefaultBootstrapConfigBuilder
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepository
import tech.zhifu.app.myhub.datastore.repository.template.CardTemplateRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.startup.StartupTask

val bootstrapModule = module {
    factory {
        Bootstrap(
            userRepository = get<UserRepository>(),
            tagRepository = get<TagRepository>(),
            collectionRepository = get<CollectionRepository>(),
            cardRepository = get<CardRepository>(),
            cardTemplateRepository = get<CardTemplateRepository>(),
            configBuilder = ::DefaultBootstrapConfigBuilder,
        )
    }
    factory<StartupTask> {
        BootstrapStartupTask(
            userRepository = get(),
            bootstrap = get(),
        )
    }
}
