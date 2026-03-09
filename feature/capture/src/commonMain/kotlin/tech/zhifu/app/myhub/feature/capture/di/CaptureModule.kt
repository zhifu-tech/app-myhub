package tech.zhifu.app.myhub.feature.capture.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.capture.CaptureViewModel

fun captureModule() = module {
    viewModel {
        CaptureViewModel(
            captureRepository = get(),
            cardRepository = get(),
            userRepository = get()
        )
    }
}
