package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_menu_settings
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.navigateToSettings

@Composable
fun MenuItemSettings(
    viewModel: DashboardViewModel,
) {
    MenuItemSettingsContent(
        onClick = viewModel::navigateToSettings
    )
}

@Composable
fun MenuItemSettingsContent(
    onClick: () -> Unit
) {
    DropdownMenuItem(
        onClick = onClick,
        text = {
            Text(
                text = stringResource(Res.string.feature_dashboard_menu_settings),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                contentDescription = null,
            )
        }
    )
}
