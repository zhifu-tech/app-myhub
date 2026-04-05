package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import kotlinx.serialization.Serializable

@Serializable
data class ActionComponentSchema(
    val type: String,
    val field: String? = null,
    val options: List<ActionOptionSchema> = emptyList(),
)

@Serializable
data class ActionOptionSchema(
    val label: String,
    val value: String,
)
