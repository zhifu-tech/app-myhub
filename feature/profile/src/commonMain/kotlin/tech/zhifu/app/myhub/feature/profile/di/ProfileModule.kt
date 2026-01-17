package tech.zhifu.app.myhub.feature.profile.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.profile.ProfileViewModel

fun profileModule() = module {
    // Profile ViewModel
    factoryOf(::ProfileViewModel)
}


