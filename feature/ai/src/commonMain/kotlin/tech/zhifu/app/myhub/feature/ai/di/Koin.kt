package tech.zhifu.app.myhub.feature.ai.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.AIViewModel

fun aiModule() = module {
    viewModel {
        AIViewModel()
    }
}
