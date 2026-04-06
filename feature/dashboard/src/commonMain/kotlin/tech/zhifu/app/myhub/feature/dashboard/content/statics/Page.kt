package tech.zhifu.app.myhub.feature.dashboard.content.statics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.statics.page.EmptyContent
import tech.zhifu.app.myhub.component.statics.page.ErrorContent
import tech.zhifu.app.myhub.component.statics.page.LoadingContent
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_empty_subtitle
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_empty_title
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_error_subtitle
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_error_title
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_loading_subtitle
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_loading_title

@Composable
fun Loading(
    contentPadding: PaddingValues,
    viewModel: DashboardViewModel
) {
    LoadingContent(
        modifier = Modifier.padding(paddingValues = contentPadding),
        title = stringResource(Res.string.feature_dashboard_loading_title),
        subTitle = stringResource(Res.string.feature_dashboard_loading_subtitle)
    )
}

@Composable
fun Error(
    contentPadding: PaddingValues,
    viewModel: DashboardViewModel
) {
    ErrorContent(
        modifier = Modifier.padding(paddingValues = contentPadding),
        title = stringResource(Res.string.feature_dashboard_error_title),
        subTitle = stringResource(Res.string.feature_dashboard_error_subtitle),
        onRetry = {
            viewModel.refresh()
        }
    )
}

@Composable
fun Empty(
    contentPadding: PaddingValues,
    viewModel: DashboardViewModel
) {
    EmptyContent(
        modifier = Modifier.padding(paddingValues = contentPadding),
        title = stringResource(Res.string.feature_dashboard_empty_title),
        subTitle = stringResource(Res.string.feature_dashboard_empty_subtitle)
    )
}
