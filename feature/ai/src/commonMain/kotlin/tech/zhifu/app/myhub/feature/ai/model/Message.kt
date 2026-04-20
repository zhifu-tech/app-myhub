package tech.zhifu.app.myhub.feature.ai.model

import org.jetbrains.compose.resources.StringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema

data class Message(
    val id: String,
    val role: Role,
    val text: String = "",
    val textRes: StringResource? = null,
    val textArgs: List<Any> = emptyList(),
    val mediaAssets: List<CaptureMediaAsset> = emptyList(),
    val editingField: Field? = null,
    val actionComponents: List<ActionComponentSchema> = emptyList(),
) {
    enum class Role {
        AI,
        USER,
        SYSTEM,
        THINKING,
    }
}
