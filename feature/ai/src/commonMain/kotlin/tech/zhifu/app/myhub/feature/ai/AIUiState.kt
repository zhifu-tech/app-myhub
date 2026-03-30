package tech.zhifu.app.myhub.feature.ai

sealed class AIUiState(
    val state: State,
) {
    object Idle : AIUiState(state = State.IDLE)

    object Loading : AIUiState(state = State.LOADING)

    data class Content(
        val messages: List<AIMessage> = emptyList(),
        val tags: List<String> = emptyList(),
        val isTagInputMode: Boolean = false,
        val newTag: String = "",
    ) : AIUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
    ) : AIUiState(state = State.ERROR)

    enum class State {
        IDLE, LOADING, CONTENT, ERROR
    }
}
