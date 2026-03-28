package tech.zhifu.app.myhub.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.Color

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

