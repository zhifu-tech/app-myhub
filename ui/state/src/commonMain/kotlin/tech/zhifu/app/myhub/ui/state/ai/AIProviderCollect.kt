package tech.zhifu.app.myhub.ui.state.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel

@Composable
fun <VM> VM.collectAIProviderState(): State<AIProvider>
    where VM : ViewModel,
          VM : AIProviderState {
    return aiProviderStateFlow
        .collectAsState(initial = AIProvider())
}
