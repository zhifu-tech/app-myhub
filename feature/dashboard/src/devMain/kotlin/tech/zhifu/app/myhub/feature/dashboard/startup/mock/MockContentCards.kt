package tech.zhifu.app.myhub.feature.dashboard.startup.mock

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.persistentListOf
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.model.ContentCardAction
import tech.zhifu.app.myhub.ui.model.ContentCardCover
import tech.zhifu.app.myhub.ui.model.ContentCardIcon

internal fun mockContentCards() = listOf(
    ContentCard(
        id = "insights",
        title = "Meeting Insights: Q3 Planning",
        summary = "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。",
        location = "青岛",
        updatedAt = 1654520000000,
        tags = persistentListOf("Meeting", "Insights", "Q3"),
        cover = ContentCardCover(
            iconKey = "visibility",
            background = ContentItemTokens.reviewBackground,
            tint = ContentItemTokens.reviewColor,
        ),
        action = ContentCardAction(
            label = ContentItemTokens.reviewLabel,
            iconKey = ContentCardIcon.Visibility.name,
            color = ContentItemTokens.reviewColor,
        ),
    ),
    ContentCard(
        id = "voice-memo",
        title = "Voice Memo #42",
        summary = "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。",
        location = "青岛",
        updatedAt = 1654520000000,
        tags = persistentListOf("Meeting", "Insights", "Q3"),
        cover = ContentCardCover(
            iconKey = "psychology",
            background = ContentItemTokens.voiceMemoCoverBackground,
            tint = ContentItemTokens.voiceMemoCoverTint,
        ),
        action = ContentCardAction(
            label = ContentItemTokens.voiceMemoActionLabel,
            iconKey = ContentCardIcon.AutoAwesome.name,
            color = ContentItemTokens.voiceMemoActionColor,
        ),
    ),
    ContentCard(
        id = "draft",
        title = "Drafting Research Paper",
        summary = "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。",
        location = "青岛",
        updatedAt = 1654520000000,
        tags = persistentListOf("Meeting", "Insights", "Q3"),
        cover = ContentCardCover(
            iconKey = "edit_note",
            background = ContentItemTokens.draftCoverBackground,
            tint = ContentItemTokens.draftCoverTint,
        ),
        action = ContentCardAction(
            label = ContentItemTokens.draftActionLabel,
            iconKey = ContentCardIcon.EditNote.name,
            color = ContentItemTokens.draftActionColor,
        ),
    ),
    ContentCard(
        id = "best-practices",
        title = "UI/UX Best Practices 2024",
        summary = "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。",
        location = "青岛",
        updatedAt = 1654520000000,
        tags = persistentListOf("Meeting", "Insights", "Q3"),
        cover = ContentCardCover(
            iconKey = "rocket_launch",
            background = ContentItemTokens.bestPracticesCoverBackground,
            tint = ContentItemTokens.bestPracticesCoverTint,
        ),
        action = ContentCardAction(
            label = ContentItemTokens.bestPracticesActionLabel,
            iconKey = ContentCardIcon.RocketLaunch.name,
            color = ContentItemTokens.bestPracticesActionColor,
        ),
    ),
    ContentCard(
        id = "quantum",
        title = "Quantum Computing Basics",
        summary = "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。",
        location = "青岛",
        updatedAt = 1654520000000,
        tags = persistentListOf("Meeting", "Insights", "Q3"),
        cover = ContentCardCover(
            iconKey = null,
            background = ContentItemTokens.quantumCoverBackground,
            url = ContentItemTokens.quantumCoverUrl,
        ),
        action = ContentCardAction(
            label = ContentItemTokens.quantumActionLabel,
            iconKey = ContentCardIcon.EditNote.name,
            color = ContentItemTokens.quantumActionColor,
        ),
    ),
    ContentCard(
        id = "weekly-sync",
        title = "Weekly Team Sync",
        summary = "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。" +
            "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。",
        location = "青岛",
        updatedAt = 1654520000000,
        tags = persistentListOf("Meeting", "Insights", "Q3"),
        cover = ContentCardCover(
            iconKey = "play_circle",
            tint = Color.White,
            background = ContentItemTokens.weeklySyncCoverBackground,
            url = ContentItemTokens.weeklySyncCoverUrl,
        ),
        action = ContentCardAction(
            label = ContentItemTokens.weeklySyncActionLabel,
            iconKey = ContentCardIcon.EditNote.name,
            color = ContentItemTokens.weeklySyncActionColor,
        ),
    ),
)

private object ContentItemColors {
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

internal object ContentItemTokens {
    val reviewIcon = Icons.Outlined.Visibility
    val reviewLabel = "Review Now"
    val reviewColor = ContentItemColors.primary

    val reviewBackground = ContentItemColors.blue50
    val voiceMemoCoverIcon = Icons.Outlined.Psychology
    val voiceMemoCoverBackground = ContentItemColors.amber50
    val voiceMemoCoverTint = ContentItemColors.amber600
    val voiceMemoActionLabel = "AI Processing..."
    val voiceMemoActionIcon = Icons.Outlined.AutoAwesome

    val voiceMemoActionColor = ContentItemColors.amber600
    val draftCoverIcon = Icons.Outlined.EditNote
    val draftCoverBackground = ContentItemColors.blue50
    val draftCoverTint = ContentItemColors.blue600
    val draftActionLabel = "Continue Inputting"
    val draftActionIcon = Icons.Outlined.Edit

    val draftActionColor = ContentItemColors.blue600
    val bestPracticesCoverIcon = Icons.Outlined.RocketLaunch
    val bestPracticesCoverBackground = ContentItemColors.teal50
    val bestPracticesCoverTint = ContentItemColors.teal600
    val bestPracticesActionLabel = "Continue Inputting"
    val bestPracticesActionIcon = Icons.Outlined.RocketLaunch

    val bestPracticesActionColor = ContentItemColors.teal600
    val quantumCoverBackground = ContentItemColors.slate100
    val quantumCoverUrl =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBeko7d4QweE0UuAM_k-N54jdwJzMNYtwe4UopJpEC8IL-8tbijJF8lmbavrtkNVbwQQgBdtzc7eaXe5VR9vp7QTb1XoyhNwV4ae5KMS6Lge5ne8CPuQ4OJEnZD6Xj1BSjiB2HbuC1e9d32D_jFcEkKtoFSEke8PBMSjoDOokLFxSLknjv325K0ZKWp5z0NdL10Yr3bnvr98mlIIFgW6_F6PVgqtvV5njmGinbyvcKS7P1Qn2h4IgR6X6Q5uoPBy7GK-7YzvsqJRIhB"
    val quantumActionLabel = "Modified 1d ago"

    val quantumActionColor = ContentItemColors.slate500
    val weeklySyncCoverBackground = ContentItemColors.slate100
    val weeklySyncCoverUrl =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBFVBQpeqyddiq-EsSvrfzzTGxiDPBB26-cGd9DCMebwXH-8k5IrY4h1TfOBmSBGiTPrsV6jLZaQ4I2JIt2XFQoKpTAW786ac1pNuf8sb5ceGOtdtHO9BVc_xKKNVORqduM9kQavIVigAl1ikvvR2ZaXWd9kMoWy142oVN8ZTxAwouO7_N9yJ1AzrhqetfRz92GIk-LgsG237pJN-vHPP4fqsO-6WEt9O-kM7YNv3jlTIooDO2feaZt3htNUSfNWo0jidycm8QZ4j8x"
    val weeklySyncActionLabel = "Modified 2 hours ago"
    val weeklySyncActionColor = ContentItemColors.slate500
}

