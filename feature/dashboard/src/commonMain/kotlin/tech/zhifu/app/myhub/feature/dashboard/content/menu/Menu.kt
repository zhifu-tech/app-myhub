package tech.zhifu.app.myhub.feature.dashboard.content.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun Menu(
    viewModel: DashboardViewModel,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    MenuContent(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        content = {
            MenuItemLayout(viewModel = viewModel, onBeforeNavigate = onDismissRequest)

            MenuItemDivider()
            MenuItemSort(viewModel = viewModel, onBeforeNavigate = onDismissRequest)

            MenuItemDivider()
            MenuItemSettings(viewModel = viewModel)
        },
    )
}

@Composable
fun MenuContent(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val groupInteractionSource = remember { MutableInteractionSource() }
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(0, 1),
            interactionSource = groupInteractionSource,
            content = content
        )
    }
}

@Composable
private fun MenuItemDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(paddingValues = MenuDefaults.HorizontalDividerPadding)
    )
}

@Composable
fun TrailingCheckedIcon(checked: Boolean) {
    if (checked) {
        Icon(
            imageVector = Icons.Filled.Check,
            modifier = Modifier.size(MenuDefaults.TrailingIconSize),
            contentDescription = null,
        )
    }
}
