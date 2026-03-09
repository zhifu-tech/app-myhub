package tech.zhifu.app.myhub.feature.card.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.card.CardDetailViewModel

fun cardDetailModule() = module {
    // CardDetail ViewModel
    // 注意：cardId 作为参数传入，需要在调用时提供
    viewModel { parameters ->
        CardDetailViewModel(
            cardId = parameters.get(),
            cardRepository = get()
        )
    }
}
