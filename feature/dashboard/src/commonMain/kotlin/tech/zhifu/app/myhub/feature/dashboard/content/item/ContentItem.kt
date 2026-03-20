package tech.zhifu.app.myhub.feature.dashboard.content.item

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
