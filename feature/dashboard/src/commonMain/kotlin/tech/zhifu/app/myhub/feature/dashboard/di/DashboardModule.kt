package tech.zhifu.app.myhub.feature.dashboard.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun dashboardModule() = module {
    // Dashboard ViewModel
    factoryOf(::DashboardViewModel)
}


