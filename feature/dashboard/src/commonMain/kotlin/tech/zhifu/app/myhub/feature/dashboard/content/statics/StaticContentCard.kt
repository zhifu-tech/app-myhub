package tech.zhifu.app.myhub.feature.dashboard.content.statics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun StaticContentCard(
    modifier: Modifier = Modifier,
    stateIndicator: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "loading-card")
    val cardShape = RoundedCornerShape(16.dp)
    val floatOffsetPx = with(LocalDensity.current) { 10.dp.toPx() }
    val floatOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -floatOffsetPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loading-card-float",
    )

    Box(
        modifier = modifier.graphicsLayer { translationY = floatOffset },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .requiredSize(width = 176.dp, height = 208.dp)
                .shadow(24.dp, cardShape, clip = false),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
                    .clickable(onClick = onClick)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = cardShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = cardShape
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StaticContentCardLine(width = 0.75f)
                    StaticContentCardLine(width = 0.5f)
                    StaticContentCardLine(width = 0.66f)
                }
                stateIndicator()
            }
        }
    }
}

@Composable
private fun StaticContentCardLine(width: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(width)
            .height(10.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(50)
            )
    )
}
