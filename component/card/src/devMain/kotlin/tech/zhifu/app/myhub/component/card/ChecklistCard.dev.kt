package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.ChecklistItem
import tech.zhifu.app.myhub.theme.AppTheme
import kotlin.time.Clock

/**
 * Preview 函数 - 浅色主题
 */
@Preview
@Composable
private fun ChecklistCardLightPreview() {
    AppTheme(darkTheme = false) {
        ChecklistCard(
            card = createSampleChecklistCard(
                title = "Weekly Tasks",
                items = listOf(
                    ChecklistItem("1", "Review code changes", false, 0),
                    ChecklistItem("2", "Update documentation", false, 1),
                    ChecklistItem("3", "Write unit tests", true, 2),
                    ChecklistItem("4", "Deploy to staging", false, 3)
                ),
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 深色主题
 */
@Preview
@Composable
private fun ChecklistCardDarkPreview() {
    AppTheme(darkTheme = true) {
        ChecklistCard(
            card = createSampleChecklistCard(
                title = "Weekly Tasks",
                items = listOf(
                    ChecklistItem("1", "Review code changes", false, 0),
                    ChecklistItem("2", "Update documentation", false, 1),
                    ChecklistItem("3", "Write unit tests", true, 2),
                    ChecklistItem("4", "Deploy to staging", false, 3)
                ),
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 收藏状态
 */
@Preview
@Composable
private fun ChecklistCardFavoritePreview() {
    AppTheme(darkTheme = false) {
        ChecklistCard(
            card = createSampleChecklistCard(
                title = "Project Milestones",
                items = listOf(
                    ChecklistItem("1", "Design phase", true, 0),
                    ChecklistItem("2", "Development phase", true, 1),
                    ChecklistItem("3", "Testing phase", false, 2),
                    ChecklistItem("4", "Deployment", false, 3)
                ),
                isFavorite = true
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 全部完成
 */
@Preview
@Composable
private fun ChecklistCardAllCompletedPreview() {
    AppTheme(darkTheme = false) {
        ChecklistCard(
            card = createSampleChecklistCard(
                title = "Daily Routine",
                items = listOf(
                    ChecklistItem("1", "Morning exercise", true, 0),
                    ChecklistItem("2", "Read for 30 minutes", true, 1),
                    ChecklistItem("3", "Work on project", true, 2),
                    ChecklistItem("4", "Evening walk", true, 3)
                ),
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 长列表
 */
@Preview
@Composable
private fun ChecklistCardLongListPreview() {
    AppTheme(darkTheme = false) {
        ChecklistCard(
            card = createSampleChecklistCard(
                title = "Feature Checklist",
                items = listOf(
                    ChecklistItem("1", "Design UI mockups", true, 0),
                    ChecklistItem("2", "Implement backend API", true, 1),
                    ChecklistItem("3", "Create database schema", true, 2),
                    ChecklistItem("4", "Write integration tests", false, 3),
                    ChecklistItem("5", "Perform code review", false, 4),
                    ChecklistItem("6", "Update user documentation", false, 5),
                    ChecklistItem("7", "Deploy to production", false, 6)
                ),
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 创建示例待办清单卡片数据
 */
private fun createSampleChecklistCard(
    title: String,
    items: List<ChecklistItem>,
    isFavorite: Boolean = false
): Card {
    val now = Clock.System.now()
    return Card(
        id = "preview-checklist-1",
        type = CardType.CHECKLIST,
        title = title,
        content = "",
        isFavorite = isFavorite,
        createdAt = now,
        updatedAt = now,
        metadata = CardMetadata(
            checklistItems = items
        )
    )
}

