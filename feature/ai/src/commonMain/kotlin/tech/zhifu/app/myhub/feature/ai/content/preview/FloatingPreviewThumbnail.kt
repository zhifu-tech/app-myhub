package tech.zhifu.app.myhub.feature.ai.content.preview

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.sharedBounds
import tech.zhifu.app.myhub.feature.preview.sharedElement

@Composable
fun FloatingPreviewThumbnail(
    previewKey: String,
    coverKey: String? = null,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier.Companion,
    onClick: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "draft_thumbnail_floating")
    val floatingOffset by infiniteTransition
        .animateFloat(
            initialValue = 0f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "draft_thumbnail_translate_y",
        )
    val interactionSource = remember { MutableInteractionSource() }
    val coverModifier = if (coverKey != null) {
        Modifier.sharedElement(
            key = coverKey,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }
    ElevatedCard(
        modifier = modifier
            .offset(y = floatingOffset.dp)
            .size(64.dp)
            .sharedBounds(
                key = previewKey,
                animatedVisibilityScope = animatedVisibilityScope,
                overlayClipShape = RoundedCornerShape(12.dp),
            )
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xBFFFFFFF),
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 10.dp,
            pressedElevation = 12.dp,
            focusedElevation = 10.dp,
            hoveredElevation = 10.dp,
        ),
    ) {
        Box(
            modifier = coverModifier
                .fillMaxWidth()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x66FFFFFF), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFF6FF))
                    .border(1.dp, Color(0xFFE0E7FF), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "Draft preview",
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(
                    modifier = Modifier
                        .size(width = 32.dp, height = 2.dp)
                        .clip(RectangleShape)
                        .background(Color(0xFFC7D2FE))
                )
                Spacer(
                    modifier = Modifier
                        .size(width = 24.dp, height = 2.dp)
                        .clip(RectangleShape)
                        .background(Color(0xFFC7D2FE))
                )
            }
        }
    }
}
