package tech.zhifu.app.myhub.component.statics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
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
    val cardShape = RoundedCornerShape(28.dp)
    val floatOffsetPx = with(LocalDensity.current) { 8.dp.toPx() }
    val floatOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -floatOffsetPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
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
                .requiredSize(width = 188.dp, height = 220.dp)
                .shadow(18.dp, cardShape, clip = false),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
                    .clickable(onClick = onClick),
                shape = cardShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f),
                ),
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StaticContentCardLine(width = 0.92f)
                        StaticContentCardLine(width = 0.68f)
                        StaticContentCardLine(width = 0.82f)
                    }
                    stateIndicator()
                }
            }
        }
    }
}

@Composable
private fun StaticContentCardLine(width: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(width)
            .height(9.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                shape = RoundedCornerShape(999.dp)
            )
    )
}
