package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import tech.zhifu.app.myhub.ui.model.ContentCard

@Immutable
sealed class DashboardUiState(
    val state: State,
) {
    @Immutable
    object Idle : DashboardUiState(state = State.IDLE)

    @Immutable
    object Loading : DashboardUiState(state = State.LOADING)

    @Immutable
    data class Content(
        val items: PersistentList<ContentCard> = persistentListOf(),
        val hasMore: Boolean = false,
        val isLoadingMore: Boolean = false,
        val errorMessage: String = "",
    ) : DashboardUiState(state = State.CONTENT) {
        companion object {
            const val PAGE_SIZE = 20
        }
    }

    @Immutable
    data class Error(
        val message: String = "",
    ) : DashboardUiState(state = State.ERROR)

    enum class State {
        IDLE, LOADING, CONTENT, ERROR
    }
}
