package tech.zhifu.app.myhub.feature.ai.model

data class Message(
    val id: String,
    val role: Role,
    val text: String,
) {
    enum class Role {
        AI,
        USER,
        SYSTEM
    }
}
