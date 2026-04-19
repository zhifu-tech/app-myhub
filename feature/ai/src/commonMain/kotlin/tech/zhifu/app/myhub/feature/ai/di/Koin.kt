package tech.zhifu.app.myhub.feature.ai.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.agent.di.agentModule
import tech.zhifu.app.myhub.feature.ai.layer.card.di.cardEngineModule
import tech.zhifu.app.myhub.feature.ai.layer.conversation.di.conversationModule
import tech.zhifu.app.myhub.feature.ai.layer.storage.di.storageModule
import tech.zhifu.app.myhub.feature.ai.layer.tool.di.toolModule
import tech.zhifu.app.myhub.feature.ai.orchestrator.CaptureOrchestrator
import tech.zhifu.app.myhub.feature.ai.orchestrator.patch.PatchApplier
import tech.zhifu.app.myhub.feature.ai.startup.AiBackgroundMaintenanceService
import tech.zhifu.app.myhub.feature.ai.startup.AiBackgroundStartupTask
import tech.zhifu.app.myhub.startup.StartupTask

fun aiModule() = module {

    includes(agentModule())
    includes(conversationModule())
    includes(cardEngineModule())
    includes(storageModule())
    includes(toolModule())

    singleOf(::PatchApplier)
    singleOf(::CaptureOrchestrator)
    singleOf(::AiBackgroundMaintenanceService)
    factoryOf(::AiBackgroundStartupTask) bind StartupTask::class

    viewModelOf(::AIViewModel)
}
