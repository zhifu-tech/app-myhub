package tech.zhifu.app.myhub

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import tech.zhifu.app.myhub.di.initKoin
import tech.zhifu.app.myhub.startup.StartupOrchestrator

object AppBootstrap {

    fun start(application: Application) {
        val koinApplication = initKoin {
            androidContext(application)
            androidLogger(level = Level.DEBUG)
        }
        koinApplication.koin.get<StartupOrchestrator>().start()
    }
}
