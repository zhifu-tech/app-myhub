package tech.zhifu.app.myhub

import io.github.vinceglb.filekit.FileKit
import tech.zhifu.app.myhub.di.initKoin
import tech.zhifu.app.myhub.startup.StartupOrchestrator

object AppBootstrap {

    fun start() {
        // 初始化 Koin 依赖注入
        val koinApplication = initKoin()
        koinApplication.koin.get<StartupOrchestrator>().start()

        FileKit.init(appId = "tech.zhifu.app.myhub")
    }
}
