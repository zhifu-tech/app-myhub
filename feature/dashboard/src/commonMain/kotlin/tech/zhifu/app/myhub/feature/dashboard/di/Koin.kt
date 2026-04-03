package tech.zhifu.app.myhub.feature.dashboard.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun dashboardModule() = module {
    viewModelOf(::DashboardViewModel)
}
