package tech.zhifu.app.myhub.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextManager
import tech.zhifu.app.myhub.feature.ai.orchestrator.CaptureOrchestrator
import tech.zhifu.app.myhub.ui.state.ai.ProviderState
import tech.zhifu.app.myhub.ui.state.ai.createAIProviderStateFlow
import tech.zhifu.app.myhub.ui.state.language.LanguageState
import tech.zhifu.app.myhub.ui.state.language.createLanguageStateFlow
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.createUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.createUserPreferencesStatFlow
import tech.zhifu.app.myhub.ui.viewmodel.ViewModelSideEffect
import tech.zhifu.app.myhub.ui.viewmodel.createSideEffectFlow
import kotlin.time.Duration.Companion.milliseconds

class AIViewModel(
    override val userRepository: UserRepository,
    private val orchestrator: CaptureOrchestrator,
    private val contextManager: ContextManager,
) : ViewModel(),
    ContainerHost<AIUiState, AISideEffect>,
    ViewModelSideEffect<AISideEffect>,
    UserState,
    UserPreferencesState,
    ProviderState,
    LanguageState {

    private var needInit = true
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val providerRoutingConfig = createAIProviderStateFlow()
    override val language = createLanguageStateFlow()

    override val container = container<AIUiState, AISideEffect>(
        initialState = AIUiState.Loading,
    ) {
        bootstrap()
    }
    override val sideEffect = createSideEffectFlow()

    private fun bootstrap() = intent {
        // 延迟数据的加载，给Loading 1s 的展示时间
        delay(1000.milliseconds)
        providerRoutingConfig
            .onEach { stateProvider ->
                orchestrator.updateProviderConfig(stateProvider)
                reduce {
                    // 非内容态，等待初始化完成
                    val state = state as? AIUiState.Content ?: return@reduce state
                    state.copy(
                        providerMode = stateProvider.mode,
                        shortcutVisible = stateProvider.shortcutVisible,
                    )
                }
                if (needInit) {
                    needInit = false
                    orchestrator.updateLanguageTag(language.value.languageTag)
                    language
                        .onEach { currentLanguage ->
                            orchestrator.updateLanguageTag(currentLanguage.languageTag)
                        }
                        .launchIn(viewModelScope)
                    // 初始化时，注册回调
                    contextManager.addContextChangeCallback { context, _ ->
                        val state = state as? AIUiState.Content ?: AIUiState.Content(
                            // 这里执行初始化，会初始化所有可能的状态，所以这里包含 PreviewMode
                            providerMode = orchestrator.providerMode(),
                            shortcutVisible = stateProvider.shortcutVisible,
                        )
                        reduce { state.copy(context = context) }
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

    fun doAction(action: String) = intent {
        val current = state as? AIUiState.Content ?: return@intent
        reduce {
            current.copy(
                input = when (action) {
                    ActionOptionType.EDIT_TITLE.value -> current.context.draft.title
                    ActionOptionType.EDIT_SUMMARY.value -> current.context.draft.summary
                    ActionOptionType.EDIT_LOCATION.value -> current.context.draft.location?.name.orEmpty()
                    else -> ""
                }
            )
        }
        orchestrator.onAction(action)
    }

    fun cancelAnalysis() = intent {
        orchestrator.cancelCurrentAnalysis()
    }
}
