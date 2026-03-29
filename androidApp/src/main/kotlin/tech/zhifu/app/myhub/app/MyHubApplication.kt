package tech.zhifu.app.myhub.app

import android.app.Application
import tech.zhifu.app.myhub.AppBootstrap

class MyHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppBootstrap.start(this)
    }
}
