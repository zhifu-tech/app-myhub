package tech.zhifu.app.myhub.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.mixed.Avatar
import tech.zhifu.app.myhub.core.navigation.AppNavKey

@Composable
fun AppNavigationBar(
    currentAppKey: NavKey,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier.Companion,
) {
    val items = listOf(
        NavItem.Dashboard,
        NavItem.New,
        NavItem.Profile,
    )
    val currentItem = when (currentAppKey) {
        is AppNavKey.Dashboard -> NavItem.Dashboard
        is AppNavKey.Profile -> NavItem.Profile
        else -> null
    }

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentItem == item,
                onClick = {
                    when (item) {
                        NavItem.Dashboard -> onNavigate(AppNavKey.Dashboard)
                        NavItem.Profile -> onNavigate(AppNavKey.Profile)
                        else -> {}
                    }
                },
                icon = {
                    when (item.screen) {
                        Screen.Profile -> {
                            Avatar(
                                size = 24.dp
                            )
                        }

                        else -> {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.labelKey),
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.labelKey),
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

