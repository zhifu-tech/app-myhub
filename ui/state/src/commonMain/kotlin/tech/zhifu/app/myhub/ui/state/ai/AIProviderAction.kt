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

fun <VH> VH.createAIProviderStateFlow(): StateFlow<AIProvider>
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {
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

fun <VH> VH.updateAIProvider(provider: AIProvider)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {

    logger.info { "updateAIProvider: $provider" }
    val userId = userPreferencesStateFlow.value.userId
    viewModelScope.launch {
        userRepository.updateUserPreferencesAiProvider(
            userId = userId,
            aiProvider = provider.toJsonText(),
        )
    }
}

fun <VH> VH.updateAIProviderMode(mode: AIProviderMode)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(mode = mode))
}

fun <VH> VH.updateAIProviderDirectEndpoint(directEndpoint: String)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(directEndpoint = directEndpoint))
}

fun <VH> VH.updateAIProviderDirectModel(directModel: String)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(directModel = directModel))
}

fun <VH> VH.updateAIProviderDirectApiKey(directApiKey: String)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(directApiKey = directApiKey))
}

fun <VH> VH.updateAIProviderTimeoutMs(timeoutMs: Long)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(timeoutMs = timeoutMs))
}

fun <VH> VH.updateAIProviderMaxRetries(maxRetries: Int)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : AIProviderState {
    updateAIProvider(aiProviderStateFlow.value.copy(maxRetries = maxRetries))
}
