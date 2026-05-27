package tech.zhifu.app.myhub.feature.ai.layer.agent.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl.DirectApiProviderClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl.RoutedProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl.ServerGatewayProviderClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.MutableProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.ProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.impl.InMemoryProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.impl.RealDirectApiHealthChecker
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.impl.RealServerGatewayHealthChecker
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.impl.DirectApiImageGenerationClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.impl.RoutedProviderImageGenerationExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.impl.ServerGatewayImageGenerationClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouter
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.impl.ConfigurableProviderRouter
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.ProviderTelemetry
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.impl.InMemoryProviderTelemetry

fun agentModule() = module {
    single<MutableProviderConfigSource> {
        InMemoryProviderConfigSource()
    }
    single<ProviderConfigSource> {
        get<MutableProviderConfigSource>()
    }
    singleOf(::RealServerGatewayHealthChecker)
    singleOf(::RealDirectApiHealthChecker)
    single<ProviderRouter> {
        ConfigurableProviderRouter(
            configSource = get<ProviderConfigSource>(),
            serverHealthChecker = get<RealServerGatewayHealthChecker>(),
            directHealthChecker = get<RealDirectApiHealthChecker>(),
        )
    }

    singleOf(::ServerGatewayProviderClient)
    singleOf(::DirectApiProviderClient)
    single<ProviderAnalysisExecutor> {
        RoutedProviderAnalysisExecutor(
            configSource = get<ProviderConfigSource>(),
            serverGatewayClient = get(),
            directApiClient = get(),
        )
    }
    singleOf(::ServerGatewayImageGenerationClient)
    singleOf(::DirectApiImageGenerationClient)
    single<ProviderImageGenerationExecutor> {
        RoutedProviderImageGenerationExecutor(
            configSource = get<ProviderConfigSource>(),
            serverGatewayClient = get(),
            directApiClient = get(),
        )
    }

    single<ProviderTelemetry> { InMemoryProviderTelemetry() }
}
