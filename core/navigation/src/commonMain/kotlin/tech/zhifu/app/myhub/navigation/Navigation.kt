package tech.zhifu.app.myhub.navigation

import androidx.compose.ui.graphics.vector.ImageVector

interface NavItem {
    val selectedIcon: ImageVector
    val unselectedIcon: ImageVector

    val iconText: String
    val titleText: String
}
