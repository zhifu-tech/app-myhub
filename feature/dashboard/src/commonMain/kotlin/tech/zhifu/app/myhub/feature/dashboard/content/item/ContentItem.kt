package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ContentItem(
    val id: String,
    val title: String,
    val cover: ContentItemCover,
    val action: ContentItemAction?,
)

sealed interface ContentItemCover {
    data class Icon(
        val icon: ImageVector,
        val background: Color,
        val tint: Color,
    ) : ContentItemCover

    data class Image(
        val url: String,
        val isVideo: Boolean,
        val placeholder: Color,
    ) : ContentItemCover
}

data class ContentItemAction(
    val label: String,
    val icon: ImageVector?,
    val color: Color,
)

object ContentItemIcons {
    val play = Icons.Outlined.PlayCircle
    val pause = Icons.Outlined.PauseCircle
    val edit = Icons.Outlined.Edit
    val launch = Icons.Outlined.RocketLaunch
    val visibility = Icons.Outlined.Visibility
    val autoAwesome = Icons.Outlined.AutoAwesome
    val autoFixHigh = Icons.Outlined.AutoFixHigh
    val editNote = Icons.Outlined.EditNote
    val psychology = Icons.Outlined.Psychology
}

