package tech.zhifu.app.myhub.profile.di

import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.profile.ProfileViewModel

fun profileModule() = module {
    // Profile ViewModel
    factoryOf(::ProfileViewModel)
}


