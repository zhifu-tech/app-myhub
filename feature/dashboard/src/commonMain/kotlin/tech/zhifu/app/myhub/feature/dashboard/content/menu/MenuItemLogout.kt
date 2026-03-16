package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@Composable
fun MenuItemLogout(
    viewModel: DashboardViewModel,
) {
    MenuItemLogoutContent(
        onActionLogout = {
            logger.debug {
                "MenuItemLogout onActionLogout"
            }
        }
    )
}

@Composable
fun MenuItemLogoutContent(
    onActionLogout: () -> Unit
) {
    DropdownMenuItem(
        onClick = onActionLogout,
        text = { Text(text = "退出登录") },
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                contentDescription = null,
            )
        }
    )
}
