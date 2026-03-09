package tech.zhifu.app.myhub.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import tech.zhifu.app.myhub.di.initKoin
import tech.zhifu.app.myhub.startup.StartupOrchestrator

class MyHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val koinApplication = initKoin {
            androidContext(this@MyHubApplication)
        }
        koinApplication.koin.get<StartupOrchestrator>().start()
    }
}
