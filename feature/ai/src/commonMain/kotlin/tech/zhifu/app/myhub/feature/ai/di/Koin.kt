package tech.zhifu.app.myhub.feature.ai.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.CaptureOrchestrator
import tech.zhifu.app.myhub.feature.ai.layer.agent.CaptureAgent
import tech.zhifu.app.myhub.feature.ai.layer.agent.ConfigurableProviderRouter
import tech.zhifu.app.myhub.feature.ai.layer.agent.DefaultCaptureAgent
import tech.zhifu.app.myhub.feature.ai.layer.agent.DirectApiProviderClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.OutputGuard
import tech.zhifu.app.myhub.feature.ai.layer.agent.PromptAssembler
import tech.zhifu.app.myhub.feature.ai.layer.agent.ProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.MutableProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.ProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.ProviderRouter
import tech.zhifu.app.myhub.feature.ai.layer.agent.ProviderTelemetry
import tech.zhifu.app.myhub.feature.ai.layer.agent.RealDirectApiHealthChecker
import tech.zhifu.app.myhub.feature.ai.layer.agent.RealServerGatewayHealthChecker
import tech.zhifu.app.myhub.feature.ai.layer.agent.ResponseParser
import tech.zhifu.app.myhub.feature.ai.layer.agent.RoutedProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.ServerGatewayProviderClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.SettingsProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.ToolPlanner
import tech.zhifu.app.myhub.feature.ai.layer.agent.InMemoryProviderTelemetry
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
import tech.zhifu.app.myhub.feature.ai.layer.storage.CaptureStorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.AiJobRecoveryManager
import tech.zhifu.app.myhub.feature.ai.layer.storage.MediaGarbageCollector
import tech.zhifu.app.myhub.feature.ai.layer.storage.MediaPostProcessExecutor
import tech.zhifu.app.myhub.feature.ai.layer.storage.RepositoryCaptureStorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolDispatcher
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolRegistry
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolRequestValidator
import tech.zhifu.app.myhub.feature.ai.startup.AiBackgroundMaintenanceService
import tech.zhifu.app.myhub.feature.ai.startup.AiBackgroundStartupTask
import tech.zhifu.app.myhub.network.createHttpClient
import tech.zhifu.app.myhub.settings.di.coreSettingsModule
import tech.zhifu.app.myhub.startup.StartupTask

fun aiModule() = module {
    includes(coreSettingsModule)

    single { PromptAssembler() }
    single { ToolPlanner() }
    single { ResponseParser() }
    single { OutputGuard() }
    single<CaptureAgent> {
        DefaultCaptureAgent(
            promptAssembler = get(),
            toolPlanner = get(),
            responseParser = get(),
            outputGuard = get(),
        )
    }
    single<MutableProviderConfigSource> { SettingsProviderConfigSource(userRepository = get()) }
    single<ProviderConfigSource> { get<MutableProviderConfigSource>() }
    single { createHttpClient() }
    single { RealServerGatewayHealthChecker(httpClient = get()) }
    single { RealDirectApiHealthChecker(httpClient = get()) }
    single<ProviderRouter> {
        ConfigurableProviderRouter(
            configSource = get<ProviderConfigSource>(),
            serverHealthChecker = get<RealServerGatewayHealthChecker>(),
            directHealthChecker = get<RealDirectApiHealthChecker>(),
        )
    }
    single { ServerGatewayProviderClient(httpClient = get()) }
    single { DirectApiProviderClient(httpClient = get()) }
    single<ProviderAnalysisExecutor> {
        RoutedProviderAnalysisExecutor(
            configSource = get<ProviderConfigSource>(),
            serverGatewayClient = get(),
            directApiClient = get(),
        )
    }
    single<ProviderTelemetry> { InMemoryProviderTelemetry() }
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
            orchestrator = get(),
        )
    }
}
