package tech.zhifu.app.myhub.feature.ai

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.feature.ai.layer.agent.ProviderMode
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationContext

class AIViewModel(
    private val orchestrator: CaptureOrchestrator,
) : ViewModel(),
    ContainerHost<AIUiState, AISideEffect> {

    private var context: ConversationContext? = null

    override val container = container<AIUiState, AISideEffect>(
        initialState = AIUiState.Loading,
    ) {
        bootstrap()
    }

    private fun bootstrap() = intent {
        val initial = orchestrator.bootstrap()
        context = initial
        reduce { initial.toUiState(input = "") }
    }

    fun updateInput(value: String) = intent {
        val current = state as? AIUiState.Content ?: return@intent
        reduce { current.copy(input = value) }
    }

    fun submitInput() = intent {
        val current = state as? AIUiState.Content ?: return@intent
        val currentContext = context ?: return@intent
        val updated = orchestrator.onInput(
            context = currentContext,
            input = current.input,
        )
        context = updated
        reduce { updated.toUiState(input = "") }
    }

    fun performQuickAction(action: String) = intent {
        val currentContext = context ?: return@intent
        val updated = orchestrator.onAction(currentContext, action)
        context = updated
        reduce { updated.toUiState(input = "") }
    }

    fun updateProviderMode(mode: ProviderMode) = intent {
        orchestrator.updateProviderMode(mode)
        val current = state as? AIUiState.Content ?: return@intent
        reduce { current.copy(providerMode = mode) }
    }

    private fun ConversationContext.toUiState(input: String): AIUiState.Content {
        return AIUiState.Content(
            captureState = state,
            sessionId = sessionId,
            messages = messages,
            draft = draft,
            missingFields = missingFields,
            actionComponents = actionComponents,
            providerMode = orchestrator.providerMode(),
            input = input,
            isPublishing = state == CaptureState.PUBLISH_CONFIRM,
        )
    }
}
