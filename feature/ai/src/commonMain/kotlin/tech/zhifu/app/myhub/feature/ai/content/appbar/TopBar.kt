package tech.zhifu.app.myhub.feature.ai.content.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import tech.zhifu.app.myhub.feature.settings.api.navigateToSettings
import tech.zhifu.app.myhub.navigation.AppNavigator

@Composable
fun TopBar(
    navigator: AppNavigator,
    hazeState: HazeState,
) {
    TopBarContent(
        hazeState = hazeState,
        onClickBack = navigator::goBack,
        actions = {
            IconButton(
                onClick = navigator::navigateToSettings
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "设置"
                )
            }
        }
    )
}

@Composable
internal fun TopBarContent(
    hazeState: HazeState,
    actions: @Composable RowScope.() -> Unit = {},
    onClickBack: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.regular(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                inputScale = HazeInputScale.Default
                progressive = HazeProgressive
                    .verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
            }
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        navigationIcon = {
            IconButton(
                onClick = onClickBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "AI 捕获",
                )
            }
        },
        title = {
            Text(
                text = "AI 捕获",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = actions
    )
}
