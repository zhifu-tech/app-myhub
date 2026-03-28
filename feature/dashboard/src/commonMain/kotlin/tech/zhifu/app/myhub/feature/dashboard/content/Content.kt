package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentGridContent
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentListContent
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectUserPreferencesFieldState
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.util.tapToClearFocus

@Composable
fun Content(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel,
    paddingValues: PaddingValues,
    previewState: PreviewState,
) {
    val items = viewModel.collectFieldAsState {
        (it as? DashboardUiState.Content)?.items
    }.value ?: emptyList()

    if (items.isEmpty()) {
        Empty(viewModel = viewModel, modifier = modifier)
        return
    }

    val layoutAsList = viewModel.collectUserPreferencesFieldState {
        it.layoutAsList
    }.value ?: true

    ContentContent(
        modifier = modifier,
        paddingValues = paddingValues,
        items = items,
        layoutAsList = layoutAsList,
        previewState = previewState,
        onLoadMore = viewModel::loadMore,
        onClickItem = previewState::show,
    )
}

@Composable
private fun ContentContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    items: List<ContentCard>,
    layoutAsList: Boolean,
    previewState: PreviewState,
    onLoadMore: () -> Unit,
    onClickItem: (ContentCard) -> Unit,
) {
    val modifier = modifier
        .fillMaxSize()
        .tapToClearFocus()
        .testTag("content-list")
        .padding(horizontal = 16.dp)
        .background(color = MaterialTheme.colorScheme.surfaceVariant)

    if (layoutAsList) {
        ContentListContent(
            modifier = modifier,
            paddingValues = paddingValues,
            items = items,
            previewState = previewState,
            onLoadMore = onLoadMore,
            onClickItem = onClickItem,
        )
    } else {
        ContentGridContent(
            modifier = modifier,
            paddingValues = paddingValues,
            items = items,
            previewState = previewState,
            onLoadMore = onLoadMore,
            onClickItem = onClickItem,
        )
    }
}
