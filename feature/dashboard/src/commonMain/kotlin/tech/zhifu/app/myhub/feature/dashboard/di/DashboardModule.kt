package tech.zhifu.app.myhub.feature.dashboard.di

import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun dashboardModule() = module {
    // Dashboard ViewModel
    factory {
        DashboardViewModel(
            cardRepository = get<CardRepository>(),
            coroutineScope = get<CoroutineScope>()
        )
    }
}


