package tech.zhifu.app.myhub.feature.dashboard.content.appbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.ui.design.resources.app_logo
import tech.zhifu.app.myhub.ui.design.resources.app_name
import tech.zhifu.app.myhub.ui.design.resources.Res as PlatformRes

@Composable
internal fun TopBar(
    modifier: Modifier,
    viewModel: DashboardViewModel,
) {
    TopBarContent(
        modifier = modifier,
        actions = {
            TopBarAvatar(
                viewModel = viewModel
            )
        }
    )
}

@Composable
internal fun TopBarContent(
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        navigationIcon = {
            Image(
                painter = painterResource(PlatformRes.drawable.app_logo),
                contentDescription = stringResource(PlatformRes.string.app_name),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = stringResource(PlatformRes.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = {
            actions()
        }
    )
}
