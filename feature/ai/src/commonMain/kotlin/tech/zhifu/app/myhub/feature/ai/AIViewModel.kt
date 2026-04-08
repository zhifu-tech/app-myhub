package tech.zhifu.app.myhub.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.ai.layer.CaptureOrchestrator
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.ProviderState
import tech.zhifu.app.myhub.ui.state.ai.createAIProviderStateFlow
import tech.zhifu.app.myhub.ui.state.ai.updateAIProviderMode
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.createUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.createUserPreferencesStatFlow

class AIViewModel(
    override val userRepository: UserRepository,
    private val orchestrator: CaptureOrchestrator,
) : ViewModel(),
    ContainerHost<AIUiState, AISideEffect>,
    UserState,
    UserPreferencesState,
    ProviderState {

    private var context: ConversationContext? = null
    private val previewState = PreviewState()
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val providerRoutingConfig = createAIProviderStateFlow()

    override val container = container<AIUiState, AISideEffect>(
        initialState = AIUiState.Loading,
    ) {
        bootstrap()
    }

    private fun bootstrap() = intent {
        providerRoutingConfig
            .onEach { stateProvider ->
                // 首先加载初始化配置
                orchestrator.updateProviderConfig(stateProvider)
                // fixme 当用户切换了AI提供商，需要重置上下文 ？？？

                // 然后加载上下文
                if (context == null) {
                    val initial = orchestrator.bootstrap()
                    context = initial
                    reduce { initial.toUiState(input = "") }
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateInput(value: String) = intent {
        val current = state as? AIUiState.Content ?: return@intent
        reduce { current.copy(input = value) }
    }

    fun submitInput() = intent {
        val current = state as? AIUiState.Content ?: return@intent
        val currentContext = context ?: return@intent
        reduce {
            current.copy(
                isThinking = true,
                thinkingText = ""
            )
        }
        val updated = orchestrator.onInput(
            context = currentContext,
            input = current.input,
            onReasoning = { thinking ->
                val currentState = state as? AIUiState.Content
                if (currentState != null) {
                    reduce {
                        currentState.copy(
                            isThinking = true,
                            thinkingText = thinking
                        )
                    }
                }
            },
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
        updateAIProviderMode(mode)
        val current = state as? AIUiState.Content ?: return@intent
        reduce { current.copy(providerMode = mode) }
    }

    private fun ConversationContext.toUiState(
        input: String
    ): AIUiState.Content = AIUiState.Content(
        conversationState = state,
        sessionId = sessionId,
        messages = messages,
        draft = draft,
        missingFields = missingFields,
        actionComponents = actionComponents,
        providerMode = orchestrator.providerMode(),
        input = input,
        isPublishing = state == ConversationState.PUBLISH_CONFIRM,
        thinkingText = "",
        isThinking = false,
        previewState = previewState,
    )
}
