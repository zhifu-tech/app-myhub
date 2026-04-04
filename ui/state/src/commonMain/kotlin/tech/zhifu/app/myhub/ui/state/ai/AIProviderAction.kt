package tech.zhifu.app.myhub.ui.state.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

fun <VM> VM.createAIProviderStateFlow(): StateFlow<AIProvider>
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {
    return userPreferencesStateFlow
        .map { prefs ->
            logger.info { "createAIProviderStateFlow: $prefs" }
            AIProvider.fromJsonText(prefs.aiProvider)
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AIProvider()
        )
}

fun <VM> VM.updateAIProvider(provider: AIProvider)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {

    logger.info { "updateAIProvider: $provider" }
    val userId = userPreferencesStateFlow.value.userId
    viewModelScope.launch {
        userRepository.updateUserPreferencesAiProvider(
            userId = userId,
            aiProvider = provider.toJsonText(),
        )
    }
}

fun <VM> VM.updateAIProviderMode(mode: AIProviderMode)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(mode = mode))
}

fun <VM> VM.updateAIProviderDirectEndpoint(directEndpoint: String)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(directEndpoint = directEndpoint))
}

fun <VM> VM.updateAIProviderDirectModel(directModel: String)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(directModel = directModel))
}

fun <VM> VM.updateAIProviderDirectApiKey(directApiKey: String)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(directApiKey = directApiKey))
}

fun <VM> VM.updateAIProviderTimeoutMs(timeoutMs: Long)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(timeoutMs = timeoutMs))
}

fun <VM> VM.updateAIProviderMaxRetries(maxRetries: Int)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(maxRetries = maxRetries))
}
