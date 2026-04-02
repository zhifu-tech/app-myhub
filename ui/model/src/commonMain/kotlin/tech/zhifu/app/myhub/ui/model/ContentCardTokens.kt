package tech.zhifu.app.myhub.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

internal object ContentCardTokens {
    val draftActionLabel = "Continue Inputting"
    val draftActionIcon = Icons.Outlined.Edit
    val draftActionColor = ContentCardColors.blue600

    val reviewIcon = Icons.Outlined.Visibility
    val reviewLabel = "Review Now"
    val reviewColor = ContentCardColors.primary

    val archivedLabel = "Archived"
    val archivedIcon = Icons.Outlined.EditNote
    val archivedColor = ContentCardColors.slate500
}

enum class ContentCardIcon(
    val key: String,
    val icon: ImageVector,
) {
    Psychology("psychology", Icons.Outlined.Psychology),
    Visibility("visibility", Icons.Outlined.Visibility),
    EditNote("edit_note", Icons.Outlined.EditNote),
    Edit("edit", Icons.Outlined.Edit),
    PlayCircle("play_circle", Icons.Outlined.PlayCircle),
    AutoAwesome("auto_awesome", Icons.Outlined.AutoAwesome),
    RocketLaunch("rocket_launch", Icons.Outlined.RocketLaunch);

    companion object {
        fun fromKey(key: String): ContentCardIcon? = entries.find { it.key == key }
    }
}

internal object ContentCardColors {
    val slate500 = Color(0xFF64748B)
    val primary = Color(0xFF137FEC)
    val blue600 = Color(0xFF2563EB)
    val purple600 = Color(0xFF7E22CE)
    val amber600 = Color(0xFFD97706)
    val teal600 = Color(0xFF0D9488)
    val purple50 = Color(0xFFFAF5FF)
    val amber50 = Color(0xFFFFFBEB)
    val blue50 = Color(0xFFEFF6FF)
    val teal50 = Color(0xFFF0FDFA)
    val slate100 = Color(0xFFF1F5F9)
}

