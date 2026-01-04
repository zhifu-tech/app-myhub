package tech.zhifu.app.myhub.dashboard.di

import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.dashboard.DashboardViewModel

fun dashboardModule() = module {
    // Dashboard ViewModel
    factoryOf(::DashboardViewModel)
}


