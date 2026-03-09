package tech.zhifu.app.myhub.feature.profile.api.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.profile.api.resource.Res
import tech.zhifu.app.myhub.feature.profile.api.resource.profile
import tech.zhifu.app.myhub.navigation.NavItem

@Serializable
object ProfileNavKey : NavKey

@Composable
fun profileNavItem() = object : NavItem {
    override val selectedIcon: ImageVector = Icons.Default.Person
    override val unselectedIcon: ImageVector = Icons.Outlined.PersonOutline
    override val iconText: String = stringResource(Res.string.profile)
    override val titleText: String = stringResource(Res.string.profile)
}
