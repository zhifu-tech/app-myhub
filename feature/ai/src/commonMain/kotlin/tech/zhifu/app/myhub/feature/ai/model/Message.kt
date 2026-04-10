package tech.zhifu.app.myhub.feature.ai.model

import org.jetbrains.compose.resources.StringResource

data class Message(
    val id: String,
    val role: Role,
    val text: String = "",
    val textRes: StringResource? = null,
    val textArgs: List<Any> = emptyList(),
) {
    enum class Role {
        AI,
        USER,
        SYSTEM
    }
}
