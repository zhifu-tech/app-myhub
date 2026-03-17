package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
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
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_menu_license
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.navigateToOpenSourceLicenses

@Composable
fun MenuItemLicense(
    viewModel: DashboardViewModel,
    onBeforeNavigate: () -> Unit,
) {
    MenuItemLicenseContent(
        onClick = {
            onBeforeNavigate()
            viewModel.navigateToOpenSourceLicenses()
        }
    )
}

@Composable
fun MenuItemLicenseContent(
    onClick: () -> Unit
) {
    DropdownMenuItem(
        onClick = onClick,
        text = {
            Text(
                text = stringResource(Res.string.feature_dashboard_menu_license),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Description,
                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                contentDescription = null,
            )
        }
    )
}
