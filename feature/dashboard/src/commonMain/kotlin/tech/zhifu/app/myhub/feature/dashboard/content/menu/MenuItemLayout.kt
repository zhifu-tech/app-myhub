package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
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
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_menu_layout_grid
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_menu_layout_list
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectUserPreferencesFieldState
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.updateUsePreferencesLayoutAsList

@Composable
fun MenuItemLayout(
    viewModel: DashboardViewModel,
    onBeforeNavigate: () -> Unit,
) {
    val layoutAsList = viewModel.collectUserPreferencesFieldState {
        it.layoutAsList
    }.value ?: true

    MenuItemLayoutContent(
        layoutAsList = layoutAsList,
        onActionLayout = {
            onBeforeNavigate()
            viewModel.updateUsePreferencesLayoutAsList(it)
        }
    )
}

@Composable
fun MenuItemLayoutContent(
    layoutAsList: Boolean,
    onActionLayout: (Boolean) -> Unit
) {
    DropdownMenuItem(
        checked = layoutAsList,
        onCheckedChange = { checked ->
            if (checked) onActionLayout(true)
        },
        text = {
            Text(
                text = stringResource(Res.string.feature_dashboard_menu_layout_list),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        shapes = MenuDefaults.itemShape(0, 5),
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ViewList,
                modifier = Modifier.size(
                    MenuDefaults.LeadingIconSize
                ),
                contentDescription = null,
            )
        },
        trailingIcon = {
            TrailingCheckedIcon(layoutAsList)
        },
    )
    DropdownMenuItem(
        checked = layoutAsList.not(),
        onCheckedChange = { checked ->
            if (checked) onActionLayout(false)
        },
        text = {
            Text(
                text = stringResource(Res.string.feature_dashboard_menu_layout_grid),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        shapes = MenuDefaults.itemShape(1, 5),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.GridView,
                modifier = Modifier.size(
                    MenuDefaults.LeadingIconSize
                ),
                contentDescription = null,
            )
        },
        trailingIcon = {
            TrailingCheckedIcon(layoutAsList.not())
        },
    )
}
