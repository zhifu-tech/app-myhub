package tech.zhifu.app.myhub

import tech.zhifu.app.myhub.di.initKoin
import tech.zhifu.app.myhub.startup.StartupOrchestrator

object AppBootstrap {

    fun start() {
        val koinApplication = initKoin()
        koinApplication.koin.get<StartupOrchestrator>().start()
    }
}
