package tech.zhifu.app.myhub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 设计稿 Rail：Explore 占位 */
@Serializable
object ExploreNavKey : NavKey

/** 设计稿 Rail：Favorites 占位 */
@Serializable
object FavoritesNavKey : NavKey

@Composable
fun ExploreNavItem() = object : NavItem {
    override val selectedIcon: ImageVector = Icons.Outlined.Explore
    override val unselectedIcon: ImageVector = Icons.Outlined.Explore
    override val iconText: String = "Explore"
    override val titleText: String = "Explore"
}

@Composable
fun FavoritesNavItem() = object : NavItem {
    override val selectedIcon: ImageVector = Icons.Filled.Star
    override val unselectedIcon: ImageVector = Icons.Outlined.Star
    override val iconText: String = "Favorites"
    override val titleText: String = "Favorites"
}

@Composable
fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
