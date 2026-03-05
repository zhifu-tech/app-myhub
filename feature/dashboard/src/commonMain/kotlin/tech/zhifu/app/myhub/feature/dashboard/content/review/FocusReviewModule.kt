package tech.zhifu.app.myhub.feature.dashboard.content.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun ReviewSectionRoute(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.collectFieldAsState { uiState ->
        (uiState as? DashboardUiState.ResultOutputCompleted)?.reviewState
    }.value ?: return

    if (state.showFocusReview && state.reviewCardsCount > 0) {
        ReviewSection(
            reviewProgress = state.reviewProgress,
            onStartReview = viewModel::startReview,
            onDismiss = viewModel::dismissFocusReview,
            modifier = modifier,
        )
    }
}

@Composable
fun ReviewSection(
    reviewProgress: ReviewProgress,
    onStartReview: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    val startReviewInteractionSource = remember { MutableInteractionSource() }
    val startReviewHovered by startReviewInteractionSource.collectIsHoveredAsState()
    val density = LocalDensity.current
    val arrowOffsetPx = with(density) { 4.dp.toPx() }
    val arrowOffsetX by animateFloatAsState(
        targetValue = if (startReviewHovered) arrowOffsetPx else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "arrow_translation_x"
    )
    Card(
        modifier = modifier.wrapContentWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.wrapContentWidth()) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 56.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressWithText(
                    progress = reviewProgress.progress,
                    text = "${reviewProgress.completed}/${reviewProgress.total}",
                    modifier = Modifier.size(64.dp)
                )
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "focus & review",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .hoverable(startReviewInteractionSource)
                            .clickable(
                                indication = null,
                                interactionSource = startReviewInteractionSource,
                                onClick = onStartReview
                            ),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { translationX = arrowOffsetX }
                        )
                    }
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CircularProgressWithText(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier.Companion
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            val sweepAngle = 360f * progress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
