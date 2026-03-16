package tech.zhifu.app.myhub.feature.dashboard.content.appbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.menu.Menu
import tech.zhifu.app.myhub.platform.resources.Res
import tech.zhifu.app.myhub.platform.resources.app_logo
import tech.zhifu.app.myhub.platform.resources.app_name

@Composable
fun TopBarAvatar(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    TopBarAvatarContent(
        onClick = {
            expanded = true
        },
        menu = {
            Menu(
                viewModel = viewModel,
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            )
        }
    )
}

@Composable
fun TopBarAvatarContent(
    onClick: () -> Unit,
    menu: @Composable () -> Unit,
) {
    TooltipBox(
        state = rememberTooltipState(),
        tooltip = { PlainTooltip { Text("Menu") } },
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Below,
        ),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
        ) {
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = stringResource(Res.string.app_name),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
            )
        }
    }
    menu()
}
