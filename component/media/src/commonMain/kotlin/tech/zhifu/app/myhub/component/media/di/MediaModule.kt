package tech.zhifu.app.myhub.component.media.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.component.media.internal.DefaultMediaPicker
import tech.zhifu.app.myhub.component.media.internal.DefaultMediaPreviewer
import tech.zhifu.app.myhub.component.media.MediaPicker
import tech.zhifu.app.myhub.component.media.MediaPreviewer

fun mediaModule() = module {
    factory<MediaPicker> { DefaultMediaPicker() }
    factory<MediaPreviewer> { DefaultMediaPreviewer() }
}
