package tech.zhifu.app.myhub.feature.dashboard.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun dashboardModule() = module {
    viewModel {
        DashboardViewModel(
            userRepository = get(),
        )
    }
}
