package tech.zhifu.app.myhub.feature.dashboard.api.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.api.resource.Res
import tech.zhifu.app.myhub.feature.dashboard.api.resource.dashboard
import tech.zhifu.app.myhub.navigation.NavItem

@Serializable
object DashboardNavKey : NavKey


@Composable
fun dashboardNavItem() = object : NavItem {
    override val selectedIcon: ImageVector = Icons.Default.Home
    override val unselectedIcon: ImageVector = Icons.Outlined.Home
    override val iconText: String = stringResource(Res.string.dashboard)
    override val titleText: String = stringResource(Res.string.dashboard)
}
