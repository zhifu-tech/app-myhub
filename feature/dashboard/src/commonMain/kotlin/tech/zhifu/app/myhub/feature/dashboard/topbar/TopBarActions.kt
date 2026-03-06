package tech.zhifu.app.myhub.feature.dashboard.topbar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_more_options
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_refresh
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_search

@Composable
internal fun TopBarActions(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val searchLabel = stringResource(Res.string.feature_dashboard_search)
    val refreshLabel = stringResource(Res.string.feature_dashboard_refresh)
    val moreOptionsLabel = stringResource(Res.string.feature_dashboard_more_options)

    TooltipIconButton(
        icon = Icons.Filled.Search,
        tooltipText = searchLabel,
        contentDescription = searchLabel,
        onClick = { /* TODO */ }
    )
    TooltipIconButton(
        icon = Icons.Filled.Refresh,
        tooltipText = refreshLabel,
        contentDescription = refreshLabel,
        content = {
            RefreshButton(
                isLoading = isRefreshing,
                onRefresh = onRefresh,
                contentDescription = refreshLabel,
            )
        }
    )
    TooltipIconButton(
        icon = Icons.Filled.MoreVert,
        tooltipText = moreOptionsLabel,
        contentDescription = moreOptionsLabel,
        onClick = { /* TODO */ }
    )
}

@Composable
private fun RefreshButton(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    contentDescription: String,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")
    val loadingRotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    IconButton(
        onClick = onRefresh,
        enabled = !isLoading,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .rotate(if (isLoading) loadingRotationAngle else 0f)
        )
    }
}

@Composable
private fun TooltipIconButton(
    icon: ImageVector,
    tooltipText: String,
    contentDescription: String,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(tooltipText) } },
        state = rememberTooltipState(),
    ) {
        if (content != null) content() else IconButton(onClick = onClick ?: {}) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}
