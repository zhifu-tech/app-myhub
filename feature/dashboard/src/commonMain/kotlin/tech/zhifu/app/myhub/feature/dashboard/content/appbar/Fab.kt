package tech.zhifu.app.myhub.feature.dashboard.content.appbar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectContentAsEmpty
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectContentAsSearching
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.design.util.rememberKeyboardOpenState

@Composable
fun Fab(
    modifier: Modifier,
    viewModel: DashboardViewModel,
) {
    val isContentEmpty by viewModel.collectContentAsEmpty()
    val isSearching by viewModel.collectContentAsSearching()
    if (isContentEmpty) {
        if (isSearching.not()) {
            return
        }
    }
    FabContent(
        modifier = modifier,
        isSearching = isSearching,
        onClickAdd = {
            logger.debug {
                "FabRoute onClickAdd is called"
            }
        },
        onClickClose = {
            logger.debug {
                "FabRoute onClickClear is called"
            }
            viewModel.search(reset = true)
        }
    )
}

@Composable
fun FabContent(
    modifier: Modifier = Modifier,
    onClickAdd: () -> Unit,
    onClickClose: () -> Unit,
    isSearching: Boolean,
) {
    val isKeyboardOpened by rememberKeyboardOpenState()
    val showAsClose = isKeyboardOpened || isSearching

    FloatingActionButton(
        onClick = if (showAsClose) {
            onClickClose
        } else {
            onClickAdd
        },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        modifier = modifier
            .height(56.dp)
            .aspectRatio(ratio = 1f, matchHeightConstraintsFirst = true)
    ) {
        FabIcon(isClose = showAsClose)
    }
}

@Composable
private fun FabIcon(
    isClose: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(targetState = isClose, label = "fab-icon")
    val closeAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        },
        label = "fab-close-alpha"
    ) { showClose ->
        if (showClose) 1f else 0f
    }
    val addAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        },
        label = "fab-add-alpha"
    ) { showClose ->
        if (showClose) 0f else 1f
    }
    val closeRotation by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        },
        label = "fab-close-rotation"
    ) { showClose ->
        if (showClose) 0f else -90f
    }
    val addRotation by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        },
        label = "fab-add-rotation"
    ) { showClose ->
        if (showClose) 90f else 0f
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    alpha = closeAlpha
                    rotationZ = closeRotation
                }
        )
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    alpha = addAlpha
                    rotationZ = addRotation
                }
        )
    }
}
