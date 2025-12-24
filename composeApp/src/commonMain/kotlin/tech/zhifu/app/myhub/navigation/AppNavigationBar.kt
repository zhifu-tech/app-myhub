package tech.zhifu.app.myhub.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.components.Avatar

/**
 * Bottom Navigation Bar for Mobile
 */
@Composable
fun AppNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    val items = listOf(
        NavItem.Dashboard,
        NavItem.New,
        NavItem.Profile,
    )
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = {
                    if (item.screen == Screen.Profile) {
                        Avatar(size = 24.dp)
                    } else {
                        Icon(item.icon, contentDescription = stringResource(item.labelKey))
                    }
                },
                label = { Text(stringResource(item.labelKey)) },
                alwaysShowLabel = true
            )
        }
    }
}

