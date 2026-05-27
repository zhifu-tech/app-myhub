package tech.zhifu.app.myhub.feature.ai.layer.card.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.card.impl.LocalCardEngine
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardFieldFormatter
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardPrePublishChecker
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardValidator

fun cardEngineModule() = module {
    singleOf(::CardFieldFormatter)
    singleOf(::CardPrePublishChecker)
    singleOf(::CardValidator)
    single<CardEngine> {
        LocalCardEngine(
            fieldFormatter = get(),
            prePublishChecker = get(),
            validator = get(),
        )
    }
}
