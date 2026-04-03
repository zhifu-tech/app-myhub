package tech.zhifu.app.myhub.ui.state.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel

@Composable
fun <VH> VH.collectAIProviderState(): State<AIProvider>
    where VH : ViewModel,
          VH : AIProviderState {
    return aiProviderStateFlow
        .collectAsState(initial = AIProvider())
}
