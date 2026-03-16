package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
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
fun MenuItemLayout(
    viewModel: DashboardViewModel
) {
    // fixme 这里需要从用户偏好 中加载配置
    var layoutAsList by rememberSaveable { mutableStateOf(true) }

    MenuItemLayoutContent(
        layoutAsList = layoutAsList,
        onActionLayout = {
            layoutAsList = it
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
        text = { Text(text = "列表") },
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
        text = { Text(text = "图标") },
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
