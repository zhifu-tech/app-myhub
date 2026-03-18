package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.Color

fun mockContentItems(): List<ContentItem> {
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

    return listOf(
        ContentItem(
            id = "insights",
            title = "Meeting Insights: Q3 Planning",
            cover = ContentItemCover.Icon(
                icon = Icons.Outlined.AutoFixHigh,
                background = purple50,
                tint = purple600,
            ),
            action = ContentItemAction(
                label = "Review Now",
                icon = Icons.Outlined.Visibility,
                color = primary,
            ),
        ),
        ContentItem(
            id = "voice-memo",
            title = "Voice Memo #42",
            cover = ContentItemCover.Icon(
                icon = Icons.Outlined.Psychology,
                background = amber50,
                tint = amber600,
            ),
            action = ContentItemAction(
                label = "AI Processing...",
                icon = Icons.Outlined.AutoAwesome,
                color = amber600,
            ),
        ),
        ContentItem(
            id = "draft",
            title = "Drafting Research Paper",
            cover = ContentItemCover.Icon(
                icon = Icons.Outlined.EditNote,
                background = blue50,
                tint = blue600,
            ),
            action = ContentItemAction(
                label = "Continue Inputting",
                icon = Icons.Outlined.Edit,
                color = blue600,
            ),
        ),
        ContentItem(
            id = "best-practices",
            title = "UI/UX Best Practices 2024",
            cover = ContentItemCover.Icon(
                icon = Icons.Outlined.RocketLaunch,
                background = teal50,
                tint = teal600,
            ),
            action = ContentItemAction(
                label = "Continue Inputting",
                icon = Icons.Outlined.RocketLaunch,
                color = teal600,
            ),
        ),
        ContentItem(
            id = "quantum",
            title = "Quantum Computing Basics",
            cover = ContentItemCover.Image(
                url = "https://lh3.googleusercontent.com/aida-public/AB6AXuBeko7d4QweE0UuAM_k-N54jdwJzMNYtwe4UopJpEC8IL-8tbijJF8lmbavrtkNVbwQQgBdtzc7eaXe5VR9vp7QTb1XoyhNwV4ae5KMS6Lge5ne8CPuQ4OJEnZD6Xj1BSjiB2HbuC1e9d32D_jFcEkKtoFSEke8PBMSjoDOokLFxSLknjv325K0ZKWp5z0NdL10Yr3bnvr98mlIIFgW6_F6PVgqtvV5njmGinbyvcKS7P1Qn2h4IgR6X6Q5uoPBy7GK-7YzvsqJRIhB",
                isVideo = false,
                placeholder = slate100,
            ),
            action = ContentItemAction(
                label = "Modified 1d ago",
                icon = null,
                color = slate500,
            ),
        ),
        ContentItem(
            id = "weekly-sync",
            title = "Weekly Team Sync",
            cover = ContentItemCover.Image(
                url = "https://lh3.googleusercontent.com/aida-public/AB6AXuBFVBQpeqyddiq-EsSvrfzzTGxiDPBB26-cGd9DCMebwXH-8k5IrY4h1TfOBmSBGiTPrsV6jLZaQ4I2JIt2XFQoKpTAW786ac1pNuf8sb5ceGOtdtHO9BVc_xKKNVORqduM9kQavIVigAl1ikvvR2ZaXWd9kMoWy142oVN8ZTxAwouO7_N9yJ1AzrhqetfRz92GIk-LgsG237pJN-vHPP4fqsO-6WEt9O-kM7YNv3jlTIooDO2feaZt3htNUSfNWo0jidycm8QZ4j8x",
                isVideo = true,
                placeholder = slate100,
            ),
            action = ContentItemAction(
                label = "Modified 2 hours ago",
                icon = null,
                color = slate500,
            ),
        ),
    )
}
