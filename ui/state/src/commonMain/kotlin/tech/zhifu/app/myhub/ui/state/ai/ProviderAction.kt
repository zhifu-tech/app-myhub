package tech.zhifu.app.myhub.ui.state.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.serializer.deserialize
import tech.zhifu.app.myhub.datastore.model.serializer.serialize
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

fun <VM> VM.createAIProviderStateFlow(): StateFlow<ProviderRoutingConfig>
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    return userPreferencesStateFlow
        .map { prefs ->
            prefs.aiProvider.deserialize<ProviderRoutingConfig>()
                .getOrNull()
        }
        .filterNotNull()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProviderRoutingConfig()
        )
}

fun <VM> VM.updateAIProvider(providerRoutingConfig: ProviderRoutingConfig)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {

    logger.info { "updateAIProvider: $providerRoutingConfig" }
    val userId = userPreferencesStateFlow.value.userId
    viewModelScope.launch {
        userRepository.updateUserPreferencesAiProvider(
            userId = userId,
            aiProvider = providerRoutingConfig.serialize().orEmpty()
        )
    }
}

fun <VM> VM.updateAIProviderMode(mode: ProviderMode)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    updateAIProvider(providerRoutingConfigStateFlow.value.copy(mode = mode))
}

fun <VM> VM.updateAIProviderDirectEndpoint(directEndpoint: String)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    updateAIProvider(providerRoutingConfigStateFlow.value.copy(directEndpoint = directEndpoint))
}

fun <VM> VM.updateAIProviderDirectModel(directModel: String)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    updateAIProvider(providerRoutingConfigStateFlow.value.copy(directModel = directModel))
}

fun <VM> VM.updateAIProviderDirectApiKey(directApiKey: String)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    updateAIProvider(providerRoutingConfigStateFlow.value.copy(directApiKey = directApiKey))
}

fun <VM> VM.updateAIProviderTimeoutMs(timeoutMs: Long)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    updateAIProvider(providerRoutingConfigStateFlow.value.copy(timeoutMs = timeoutMs))
}

fun <VM> VM.updateAIProviderMaxRetries(maxRetries: Int)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    updateAIProvider(providerRoutingConfigStateFlow.value.copy(maxRetries = maxRetries))
}
