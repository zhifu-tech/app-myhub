package tech.zhifu.app.myhub.feature.dashboard.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun dashboardModule() = module {
    // Dashboard ViewModel
    viewModel {
        DashboardViewModel(
            cardRepository = get(),
            collectionRepository = get(),
            userRepository = get(),
            authSessionCoordinator = get(),
        )
    }
}
