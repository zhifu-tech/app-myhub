package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_menu_sort_by_date
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_menu_sort_by_name
import tech.zhifu.app.myhub.ui.state.layout.collectAsSortAsDateStateWithLifecycle
import tech.zhifu.app.myhub.ui.state.layout.updateSortAsDate

@Composable
fun MenuItemSort(
    viewModel: DashboardViewModel,
    onBeforeNavigate: () -> Unit,
) {
    val sortAsDate by viewModel.layout.collectAsSortAsDateStateWithLifecycle()
    MenuItemSortContent(
        sortAsDate = sortAsDate,
        onActionSort = {
            onBeforeNavigate()
            viewModel.updateSortAsDate(it)
        }
    )
}

@Composable
fun MenuItemSortContent(
    sortAsDate: Boolean,
    onActionSort: (Boolean) -> Unit,
) {
    DropdownMenuItem(
        checked = sortAsDate,
        onCheckedChange = { checked ->
            if (checked) onActionSort(true)
        },
        text = {
            Text(
                text = stringResource(Res.string.feature_dashboard_menu_sort_by_date),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        shapes = MenuDefaults.itemShape(0, 5),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                modifier = Modifier.size(
                    MenuDefaults.LeadingIconSize
                ),
                contentDescription = null,
            )
        },
        trailingIcon = {
            TrailingCheckedIcon(sortAsDate)
        },
    )
    DropdownMenuItem(
        checked = sortAsDate.not(),
        onCheckedChange = { checked ->
            if (checked) onActionSort(false)
        },
        text = {
            Text(
                text = stringResource(Res.string.feature_dashboard_menu_sort_by_name),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        shapes = MenuDefaults.itemShape(1, 5),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.SortByAlpha,
                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                contentDescription = null,
            )
        },
        trailingIcon = {
            TrailingCheckedIcon(sortAsDate.not())
        },
    )
}
