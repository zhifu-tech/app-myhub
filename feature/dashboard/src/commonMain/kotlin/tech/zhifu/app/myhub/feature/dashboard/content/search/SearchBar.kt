package tech.zhifu.app.myhub.feature.dashboard.content.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.ui.viewmodel.CollectPredicatedSharedSideEffect
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.sideEffect
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun SearchBar(
    modifier: Modifier,
    viewModel: DashboardViewModel
) {
    val focusManager = LocalFocusManager.current
    viewModel.sideEffect.CollectPredicatedSharedSideEffect(
        predicate = { it is DashboardSideEffect.ResetSearch }
    ) {
        focusManager.clearFocus()
    }

    val isContentEmpty by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.items?.isEmpty() ?: true
    }
    val isSearching by viewModel.searchState.collectAsSearchingStateWithLifecycle()

    val showSearchingBar = !(isContentEmpty && !isSearching)

    val query by viewModel.searchState.collectAsState()
    val queryState = remember { mutableStateOf(query) }

    AnimatedVisibility(
        modifier = modifier,
        visible = showSearchingBar,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        SearchBarContent(
            query = queryState.value,
            onQueryChange = {
                queryState.value = it
                viewModel.search(it)
            },
            onSearch = { focusManager.clearFocus() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SearchBarContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isFocused = remember { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = if (isFocused.value) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surface/*.copy(alpha = 0.95f)*/
        }
    )

    val scale by animateFloatAsState(
        targetValue = if (isFocused.value) 1.02f else 1f,
        label = "scale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(999.dp),
        tonalElevation = if (isFocused.value) 4.dp else 2.dp,
        shadowElevation = if (isFocused.value) 8.dp else 4.dp,
        color = containerColor
    ) {
        SearchInputCore(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {
                focusManager.clearFocus()
                onSearch()
            },
            onFocusChange = { isFocused.value = it }
        )
    }
}
