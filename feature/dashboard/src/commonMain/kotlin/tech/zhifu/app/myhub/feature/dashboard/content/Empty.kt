package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContent
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentCard
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentState
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentTexts
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticQuote
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_empty_subtitle
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_empty_title

@Composable
fun Empty(
    modifier: Modifier = Modifier
) {
    val title = stringResource(Res.string.feature_dashboard_empty_title)
    val subtitle = stringResource(Res.string.feature_dashboard_empty_subtitle)

    StaticContent(
        modifier = modifier,
        contentCard = {
            StaticContentCard(
                onClick = {

                },
                stateIndicator = {
                    StaticContentState(
                        stateIndicator = { modifier ->
                            EmptyIndicator(modifier)
                        }
                    )
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
private fun EmptyIndicator(modifier: Modifier) {
    Icon(
        imageVector = Icons.Outlined.Add,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = modifier.size(36.dp)
    )
}
