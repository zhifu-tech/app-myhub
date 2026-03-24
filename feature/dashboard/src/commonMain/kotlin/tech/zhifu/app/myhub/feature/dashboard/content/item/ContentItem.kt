package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ContentItem(
    val id: String,
    val title: String,
    val note: String,
    val location: String,
    val updatedTimeMs: Long,
    val tags: List<String>,
    val isVideo: Boolean,
    val cover: ContentItemCover,
    val action: ContentItemAction,
)

data class ContentItemCover(
    val icon: ImageVector? = null,
    val background: Color,
    val tint: Color? = null,
    val url: String? = null,
)

data class ContentItemAction(
    val label: String,
    val icon: ImageVector,
    val color: Color,
)
