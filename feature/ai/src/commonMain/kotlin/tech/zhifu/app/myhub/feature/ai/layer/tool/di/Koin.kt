package tech.zhifu.app.myhub.feature.ai.layer.tool.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandDispatcher
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandRegistry
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.registery.impl.ToolCommandRegistryImpl
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.validator.impl.ToolCommandValidatorImpl

fun toolModule() = module {
    single<ToolCommandValidator> {
        ToolCommandValidatorImpl()
    }

    single<ToolCommandRegistry> {
        ToolCommandRegistryImpl(
            cardEngine = get(),
            storageGateway = get(),
            mediaPicker = get(),
        )
    }

    singleOf(::ToolCommandDispatcher)
}
