package tech.zhifu.app.myhub.ui.state.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel

@Composable
fun <VM> VM.collectLanguage(): State<Language>
    where VM : ViewModel,
          VM : LanguageState {
    return languageStateFlow
        .collectAsState(initial = Language.ZH_CN)
}
