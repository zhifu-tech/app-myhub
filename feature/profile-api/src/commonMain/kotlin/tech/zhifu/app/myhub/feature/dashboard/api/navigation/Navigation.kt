package tech.zhifu.app.myhub.feature.dashboard.api.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.core.navigation.NavItem
import tech.zhifu.app.myhub.feature.profile.api.resource.Res
import tech.zhifu.app.myhub.feature.profile.api.resource.profile

@Serializable
object ProfileNavKey : NavKey

@Composable
fun ProfileNavItem() = object : NavItem {
    override val selectedIcon: ImageVector = Icons.Default.Dashboard
    override val unselectedIcon: ImageVector = Icons.Outlined.Dashboard
    override val iconText: String = stringResource(Res.string.profile)
    override val titleText: String = stringResource(Res.string.profile)
}
