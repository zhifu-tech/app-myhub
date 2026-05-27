package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health

data class ProviderHealth(
    val healthy: Boolean,
    val reason: String? = null,
)
