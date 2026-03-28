package tech.zhifu.app.myhub.feature.dashboard.di

import org.koin.dsl.bind
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.dashboard.startup.mock.MockContentCardStartupTask
import tech.zhifu.app.myhub.startup.StartupTask

fun dashboardDevModule() = module {
    factory {
        MockContentCardStartupTask(
            userRepository = get(),
            cardRepository = get(),
        )
    } bind StartupTask::class
}
