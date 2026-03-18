package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun Content(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel,
) {
    ContentContent(
        modifier = modifier,
        onClickItem = {
//            viewModel.navigateToAppDetail(it)
        }
    )
}

@Composable
fun ContentContent(
    modifier: Modifier,
    onClickItem: () -> Unit
) {

}
