package tech.zhifu.app.myhub.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.ai.orchestrator.CaptureOrchestrator
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.ProviderState
import tech.zhifu.app.myhub.ui.state.ai.createAIProviderStateFlow
import tech.zhifu.app.myhub.ui.state.ai.updateAIProviderMode
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.createUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.createUserPreferencesStatFlow
import tech.zhifu.app.myhub.ui.viewmodel.ViewModelSideEffect
import tech.zhifu.app.myhub.ui.viewmodel.createSideEffectFlow

class AIViewModel(
    override val userRepository: UserRepository,
    private val orchestrator: CaptureOrchestrator,
    private val contextManager: ContextManager,
) : ViewModel(),
    ContainerHost<AIUiState, AISideEffect>,
    ViewModelSideEffect<AISideEffect>,
    UserState,
    UserPreferencesState,
    ProviderState {

    private var needInit = true
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val providerRoutingConfig = createAIProviderStateFlow()

    override val container = container<AIUiState, AISideEffect>(
        initialState = AIUiState.Loading,
    ) {
        bootstrap()
    }
    override val sideEffect = createSideEffectFlow()

    private fun bootstrap() = intent {
        providerRoutingConfig
            .onEach { stateProvider ->
                orchestrator.updateProviderConfig(stateProvider)
                reduce {
                    // 非内容态，等待初始化完成
                    val state = state as? AIUiState.Content ?: return@reduce state
                    state.copy(
                        providerMode = stateProvider.mode,
                    )
                }
                if (needInit) {
                    needInit = false
                    // 初始化时，注册回调
                    contextManager.addContextChangeCallback { context, _ ->
                        val state = state as? AIUiState.Content ?: AIUiState.Content(
                            // 这里执行初始化，会初始化所有可能的状态，所以这里包含 PreviewMode
                            providerMode = orchestrator.providerMode(),
                        )
                        reduce {
                            state.copy(
                                conversationState = context.state,
                                messages = context.messages,
                                draft = context.draft,
                                missingFields = context.missingFields,
                                actionComponents = context.actionComponents,
                                // 更新推理状态
                                reasoningStatus = context.reasoningStatus,
                                reasoningText = context.reasoningText,
                            )
                        }
                    }

                    orchestrator.bootstrap()
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
        val userInput = current.input
        if (userInput.trim().isBlank()) return@intent
        orchestrator.onInput(
            input = userInput,
        )
    }

    fun performQuickAction(action: String) = intent {
        orchestrator.onAction(action)
//        val currentContext = context ?: return@intent
//        val updated = orchestrator.onAction(currentContext, action)
//        context = updated
//        reduce {
//            updated.toUiState(
//                input = prefilledInputForAction(
//                    action = action,
//                    context = updated,
//                )
//            )
//        }
    }

    fun updateProviderMode(mode: ProviderMode) = intent {
        updateAIProviderMode(mode)
        val current = state as? AIUiState.Content ?: return@intent
        reduce { current.copy(providerMode = mode) }
    }

//    private fun ConversationContext.toUiState(
//        input: String
//    ): AIUiState.Content = AIUiState.Content(
//        conversationState = state,
//        sessionId = sessionId,
//        messages = messages,
//        draft = draft,
//        inputField = inputField,
//        missingFields = missingFields,
//        actionComponents = actionComponents,
//        providerMode = orchestrator.providerMode(),
//        input = input,
//        isPublishing = state == ConversationState.PUBLISH_CONFIRM,
//        thinkingText = "",
//        isThinking = false,
//        previewState = previewState,
//    )

    private fun prefilledInputForAction(
        action: String,
        context: ConversationContext,
    ): String = when (action) {
        ActionOptionType.EDIT_TITLE.value -> context.draft?.title.orEmpty()
        ActionOptionType.EDIT_SUMMARY.value -> context.draft?.summary.orEmpty()
        ActionOptionType.EDIT_LOCATION.value -> context.draft?.location?.name.orEmpty()
        else -> ""
    }
}
