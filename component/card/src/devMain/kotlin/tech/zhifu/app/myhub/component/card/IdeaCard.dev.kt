package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.theme.AppTheme
import kotlin.time.Clock

/**
 * Preview 函数 - 浅色主题
 */
@Preview
@Composable
private fun IdeaCardLightPreview() {
    AppTheme(darkTheme = false) {
        IdeaCard(
            card = createSampleIdeaCard(
                content = "Add dark mode support to the settings screen",
                priority = "high",
                status = "new",
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
private fun IdeaCardDarkPreview() {
    AppTheme(darkTheme = true) {
        IdeaCard(
            card = createSampleIdeaCard(
                content = "Add dark mode support to the settings screen",
                priority = "high",
                status = "new",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 进行中状态
 */
@Preview
@Composable
private fun IdeaCardInProgressPreview() {
    AppTheme(darkTheme = false) {
        IdeaCard(
            card = createSampleIdeaCard(
                content = "Implement user authentication flow",
                priority = "medium",
                status = "in-progress",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 已完成状态
 */
@Preview
@Composable
private fun IdeaCardCompletedPreview() {
    AppTheme(darkTheme = false) {
        IdeaCard(
            card = createSampleIdeaCard(
                content = "Set up CI/CD pipeline",
                priority = "low",
                status = "completed",
                isFavorite = true
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 长文本
 */
@Preview
@Composable
private fun IdeaCardLongTextPreview() {
    AppTheme(darkTheme = false) {
        IdeaCard(
            card = createSampleIdeaCard(
                content = "Create a comprehensive onboarding flow that guides new users through " +
                    "the key features of the app, including tutorials, tips, and interactive " +
                    "demonstrations to help them get started quickly.",
                priority = "high",
                status = "new",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 创建示例想法卡片数据
 */
private fun createSampleIdeaCard(
    content: String,
    priority: String,
    status: String,
    isFavorite: Boolean = false
): Card {
    val now = Clock.System.now()
    return Card(
        id = "preview-idea-1",
        type = CardType.IDEA,
        content = content,
        isFavorite = isFavorite,
        createdAt = now,
        updatedAt = now,
        metadata = CardMetadata(
            ideaPriority = priority,
            ideaStatus = status
        )
    )
}

