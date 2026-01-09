package tech.zhifu.app.myhub.carddetail.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.carddetail.CardDetailViewModel

fun cardDetailModule() = module {
    // CardDetail ViewModel
    // 注意：cardId 作为参数传入，需要在调用时提供
    factoryOf(::CardDetailViewModel)
}
