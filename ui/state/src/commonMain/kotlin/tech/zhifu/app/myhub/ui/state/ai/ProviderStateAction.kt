package tech.zhifu.app.myhub.ui.state.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.serializer.deserialize
import tech.zhifu.app.myhub.datastore.model.serializer.serialize
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

fun <VM> VM.createAIProviderStateFlow(): StateFlow<ProviderRoutingConfig>
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    return userPreferencesStateFlow
        .mapNotNull { prefs ->
            prefs.aiProvider.deserialize<ProviderRoutingConfig>()
                .getOrNull()
        }
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
    updateAIProvider(providerRoutingConfig.value.copy(mode = mode))
}

fun <VM> VM.updateAIProviderShortcutVisible(visible: Boolean)
    where VM : ViewModel,
          VM : UserPreferencesState,
          VM : ProviderState {
    updateAIProvider(providerRoutingConfig.value.copy(shortcutVisible = visible))
}
