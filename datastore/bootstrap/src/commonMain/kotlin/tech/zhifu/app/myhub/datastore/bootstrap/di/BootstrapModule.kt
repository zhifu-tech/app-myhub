package tech.zhifu.app.myhub.datastore.bootstrap.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.bootstrap.startup.BootstrapStartupTask
import tech.zhifu.app.myhub.startup.StartupTask

fun bootstrapModule() = module {
    factoryOf(::Bootstrap)
    factory {
        BootstrapStartupTask(
            userRepository = get(),
            bootstrap = get(),
        )
    } bind StartupTask::class
}
