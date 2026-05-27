package tech.zhifu.app.myhub.feature.dashboard.content.search

import kotlinx.coroutines.flow.MutableStateFlow

interface SearchState {
    val searchState: MutableStateFlow<String>
}
