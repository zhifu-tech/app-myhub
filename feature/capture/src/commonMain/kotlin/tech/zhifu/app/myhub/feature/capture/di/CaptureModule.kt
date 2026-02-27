package tech.zhifu.app.myhub.feature.capture.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.capture.CaptureViewModel

fun captureModule() = module {
    factory {
        CaptureViewModel(
            coroutineScope = get(),
            captureRepository = get(),
            cardRepository = get(),
            userRepository = get()
        )
    }
}
