package tech.zhifu.app.myhub.startup.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.startup.AppStartupScope
import tech.zhifu.app.myhub.startup.StartupOrchestrator
import tech.zhifu.app.myhub.startup.StartupTask

fun startupModule() = module {
    factory { AppStartupScope() }
    factory {
        StartupOrchestrator(
            appStartupScope = get(),
            tasks = getAll<StartupTask>(),
        )
    }
}
