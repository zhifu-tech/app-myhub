package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContent
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentCard
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentState
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticContentTexts
import tech.zhifu.app.myhub.feature.dashboard.content.statics.StaticQuote
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.drawables.MaterialSymbolsProgress_activity
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_loading_subtitle
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_loading_title

@Composable
fun Loading(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    StaticContent(
        modifier = modifier,
        contentCard = {
            StaticContentCard(
                stateIndicator = {
                    StaticContentState(
                        stateIndicator = { modifier ->
                            LoadingIndicator(modifier)
                        }
                    )
                }
            )
        },
        contentTexts = { isLandscape ->
            StaticContentTexts(
                title = stringResource(Res.string.feature_dashboard_loading_title),
                subtitle = stringResource(Res.string.feature_dashboard_loading_subtitle),
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
private fun LoadingIndicator(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "loading-spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
        ),
        label = "loading-spinner-rotation",
    )
    Icon(
        imageVector = MaterialSymbolsProgress_activity,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = modifier.size(36.dp)
            .graphicsLayer { rotationZ = rotation },
    )
}
