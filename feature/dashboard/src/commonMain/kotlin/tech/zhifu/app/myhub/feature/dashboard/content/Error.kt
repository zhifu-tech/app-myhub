package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContent
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentCard
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentState
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentTexts
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticQuote
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_error_subtitle
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_error_title

@Composable
fun Error(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val title = stringResource(Res.string.feature_dashboard_error_title)
    val subtitle = stringResource(Res.string.feature_dashboard_error_subtitle)

    StaticContent(
        modifier = modifier,
        contentCard = {
            StaticContentCard(
                stateIndicator = {
                    StaticContentState(
                        stateIndicator = { modifier ->
                            ErrorIndicator(modifier)
                        }
                    )
                },
                onClick = {
                    viewModel.refresh()
                }
            )
        },
        contentTexts = { isLandscape ->
            StaticContentTexts(
                title = title,
                subtitle = subtitle,
                staticContentQuote = {
                    if (isLandscape.not()) {
                        StaticQuote()
                    }
                }
            )
        },
    )
}

@Composable
private fun ErrorIndicator(modifier: Modifier) {
    Icon(
        imageVector = Icons.Outlined.Refresh,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = modifier.size(36.dp)
    )
}
