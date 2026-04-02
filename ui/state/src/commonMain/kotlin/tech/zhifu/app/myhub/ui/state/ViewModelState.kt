package tech.zhifu.app.myhub.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

interface ViewModelState {
    fun viewModelScope(): CoroutineScope =
        (this as ViewModel).viewModelScope
}
