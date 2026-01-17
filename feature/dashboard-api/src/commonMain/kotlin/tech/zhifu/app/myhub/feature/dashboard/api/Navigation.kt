package tech.zhifu.app.myhub.feature.dashboard.api

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.core.navigation.NavItem
import tech.zhifu.app.myhub.feature.dashboard.api.resource.Res
import tech.zhifu.app.myhub.feature.dashboard.api.resource.dashboard

@Serializable
object DashboardNavKey : NavKey

@Composable
fun DashboardNavItem() = object : NavItem {
    override val selectedIcon: ImageVector = Icons.Default.Dashboard
    override val unselectedIcon: ImageVector = Icons.Outlined.Dashboard
    override val iconText: String = stringResource(Res.string.dashboard)
    override val titleText: String = stringResource(Res.string.dashboard)
}
