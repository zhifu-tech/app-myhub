package tech.zhifu.app.myhub.feature.profile.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.profile.ProfileViewModel

fun profileModule() = module {
    // Profile ViewModel
    viewModel { ProfileViewModel() }
}

