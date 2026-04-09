package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.preview.resources.Res
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_float_thumbnail_description
import tech.zhifu.app.myhub.feature.preview.sharedBounds
import tech.zhifu.app.myhub.feature.preview.sharedElement

@Composable
fun PreviewFloatThumbnail(
    previewKey: String,
    coverKey: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier.Companion,
    onClick: () -> Unit = {},
) {
    val density = LocalDensity.current
    val floatingOffset by rememberInfiniteTransition(label = "draft_thumbnail_floating")
        .animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "draft_thumbnail_translate_y",
        )
    val interactionSource = remember { MutableInteractionSource() }
    ElevatedCard(
        modifier = modifier
            .requiredSize(64.dp)
            .sharedBounds(
                key = previewKey,
                animatedVisibilityScope = animatedVisibilityScope,
                overlayClipShape = MaterialTheme.shapes.medium,
            )
            .graphicsLayer {
                translationY = with(density) { floatingOffset.dp.toPx() }
            }
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 10.dp,
            pressedElevation = 12.dp,
            focusedElevation = 10.dp,
            hoveredElevation = 10.dp,
        ),
    ) {
        Box(
            modifier = Modifier
                .sharedElement(
                    key = coverKey,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .fillMaxSize()
                .clip(shape = MaterialTheme.shapes.small)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    shape = MaterialTheme.shapes.small,
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = MaterialTheme.shapes.small)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = stringResource(Res.string.feature_preview_float_thumbnail_description),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(
                    modifier = Modifier
                        .size(width = 32.dp, height = 2.dp)
                        .clip(RectangleShape)
                        .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                )
                Spacer(
                    modifier = Modifier
                        .size(width = 24.dp, height = 2.dp)
                        .clip(RectangleShape)
                        .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                )
            }
        }
    }
}
