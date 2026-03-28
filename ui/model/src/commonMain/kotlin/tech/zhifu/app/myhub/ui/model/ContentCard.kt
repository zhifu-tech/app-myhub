package tech.zhifu.app.myhub.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ContentCard(
    val id: String,
    val title: String,
    val summary: String,
    val location: String,
    val updatedAt: Long,
    val tags: List<String>,
    val cover: ContentCardCover,
    val action: ContentCardAction,
)

data class ContentCardCover(
    val iconKey: String? = null,
    val icon: ImageVector? = null,
    val background: Color,
    val tint: Color? = null,
    val url: String? = null,
)

data class ContentCardAction(
    val label: String,
    val icon: ImageVector,
    val color: Color,
)
