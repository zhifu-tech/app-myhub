package tech.zhifu.app.myhub.feature.ai.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.CaptureOrchestrator
import tech.zhifu.app.myhub.feature.ai.layer.agent.di.agentModule
import tech.zhifu.app.myhub.feature.ai.layer.cardengine.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.cardengine.CardFieldFormatter
import tech.zhifu.app.myhub.feature.ai.layer.cardengine.CardPrePublishChecker
import tech.zhifu.app.myhub.feature.ai.layer.cardengine.CardValidator
import tech.zhifu.app.myhub.feature.ai.layer.cardengine.LocalCardEngine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ActionPlanner
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ContextManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationEngine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationStateMachine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.SlotManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.StateGuard
import tech.zhifu.app.myhub.feature.ai.layer.storage.AiJobRecoveryManager
import tech.zhifu.app.myhub.feature.ai.layer.storage.CaptureStorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.MediaGarbageCollector
import tech.zhifu.app.myhub.feature.ai.layer.storage.MediaPostProcessExecutor
import tech.zhifu.app.myhub.feature.ai.layer.storage.RepositoryCaptureStorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolDispatcher
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolRegistry
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolRequestValidator
import tech.zhifu.app.myhub.feature.ai.startup.AiBackgroundMaintenanceService
import tech.zhifu.app.myhub.feature.ai.startup.AiBackgroundStartupTask
import tech.zhifu.app.myhub.network.createHttpClient
import tech.zhifu.app.myhub.startup.StartupTask

fun aiModule() = module {
    single { createHttpClient() }
    includes(agentModule())

    single {
        AiBackgroundMaintenanceService(
            mediaPostProcessExecutor = get(),
            mediaGarbageCollector = get(),
            providerTelemetry = get(),
            analyticsService = get(),
        )
    }
    factory {
        AiBackgroundStartupTask(
            service = get(),
        )
    } bind StartupTask::class

    single { ConversationStateMachine() }
    single { SlotManager() }
    single { ActionPlanner() }
    single { ContextManager() }
    single { StateGuard() }
    single {
        ConversationEngine(
            stateMachine = get(),
            slotManager = get(),
            actionPlanner = get(),
            contextManager = get(),
        )
    }

    single { CardFieldFormatter() }
    single { CardPrePublishChecker() }
    single { CardValidator(fieldFormatter = get()) }
    single<CardEngine> {
        LocalCardEngine(
            fieldFormatter = get(),
            prePublishChecker = get(),
            validator = get(),
        )
    }

    single<CaptureStorageGateway> {
        RepositoryCaptureStorageGateway(
            userRepository = get(),
            cardRepository = get(),
            captureLocalRepository = get(),
            mediaPostProcessExecutor = get(),
            mediaGarbageCollector = get(),
        )
    }
    single { MediaPostProcessExecutor(captureLocalRepository = get()) }
    single { AiJobRecoveryManager(captureLocalRepository = get(), mediaPostProcessExecutor = get()) }
    single { MediaGarbageCollector(captureLocalRepository = get()) }

    single {
        ToolRequestValidator()
    }

    single {
        ToolRegistry(
            cardEngine = get(),
            storageGateway = get(),
            mediaPicker = get(),
        )
    }

    single {
        ToolDispatcher(
            registry = get(),
            validator = get(),
        )
    }

    single {
        CaptureOrchestrator(
            providerRouter = get(),
            conversationEngine = get(),
            stateGuard = get(),
            captureAgent = get(),
            providerAnalysisExecutor = get(),
            providerConfigSource = get(),
            providerTelemetry = get(),
            toolDispatcher = get(),
            storageGateway = get(),
        )
    }

    viewModel {
        AIViewModel(
            userRepository = get(),
            orchestrator = get(),
        )
    }
}
