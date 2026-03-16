package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun MenuItemSort(
    viewModel: DashboardViewModel,
) {
    // fixme 这里需要从用户偏好 中加载配置
    var sortAsDate by rememberSaveable { mutableStateOf(true) }

    MenuItemSortContent(
        sortAsDate = sortAsDate,
        onActionSort = { sortAsDate = it }
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
        text = { Text(text = "按日期排序") },
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
        text = { Text(text = "按日名称排序") },
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
