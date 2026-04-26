package tech.zhifu.app.myhub.component.statics.page

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.component.statics.StaticContent
import tech.zhifu.app.myhub.component.statics.StaticContentCard
import tech.zhifu.app.myhub.component.statics.StaticContentState
import tech.zhifu.app.myhub.component.statics.StaticContentTexts
import tech.zhifu.app.myhub.component.statics.StaticQuote

@Composable
fun ErrorContent(
    modifier: Modifier,
    title: String,
    subTitle: String? = null,
    onRetry: () -> Unit,
) {
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
                onClick = onRetry
            )
        },
        contentTexts = { isLandscape ->
            StaticContentTexts(
                title = title,
                subtitle = subTitle,
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
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
        modifier = modifier.size(32.dp)
    )
}
