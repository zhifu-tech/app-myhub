package tech.zhifu.app.myhub.ui.state.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel

@Composable
fun <VM> VM.collectAIProviderState(): State<ProviderRoutingConfig>
    where VM : ViewModel,
          VM : ProviderState {
    return providerRoutingConfigStateFlow
        .collectAsState(initial = ProviderRoutingConfig())
}
