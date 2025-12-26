package tech.zhifu.app.myhub.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.di.initKoin

class MyHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 设置 API 基础 URL（通过反射或依赖注入）
        // 注意：由于模块依赖关系，这里暂时通过系统属性传递
        System.setProperty("myhub.api.base.url", AppBuildConfig.apiBaseUrl)

        initKoin {
            androidContext(this@MyHubApplication)
        }
    }
}
