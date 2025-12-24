package tech.zhifu.app.myhub

import android.app.Application
import org.koin.android.ext.koin.androidContext
import tech.zhifu.app.myhub.di.initKoin

class MyHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MyHubApplication)
        }
    }
}
