package zhifu.app.myhub.component.card

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.component.card.ArticleCard
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.theme.AppTheme

/**
 * Preview 函数 - 浅色主题
 */
@Preview
@Composable
private fun ArticleCardLightPreview() {
    AppTheme(darkTheme = false) {
        ArticleCard(
            card = createSampleArticleCard(
                title = "Understanding Kotlin Multiplatform",
                summary = "Kotlin Multiplatform allows you to share code between different platforms while maintaining native performance.",
                url = "https://kotlinlang.org/docs/multiplatform.html",
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
private fun ArticleCardDarkPreview() {
    AppTheme(darkTheme = true) {
        ArticleCard(
            card = createSampleArticleCard(
                title = "Understanding Kotlin Multiplatform",
                summary = "Kotlin Multiplatform allows you to share code between different platforms while maintaining native performance.",
                url = "https://kotlinlang.org/docs/multiplatform.html",
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
private fun ArticleCardFavoritePreview() {
    AppTheme(darkTheme = false) {
        ArticleCard(
            card = createSampleArticleCard(
                title = "Compose Multiplatform Best Practices",
                summary = "Learn the best practices for building cross-platform UIs with Compose Multiplatform.",
                url = "https://www.jetbrains.com/lp/compose-multiplatform/",
                isFavorite = true
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 长摘要
 */
@Preview
@Composable
private fun ArticleCardLongSummaryPreview() {
    AppTheme(darkTheme = false) {
        ArticleCard(
            card = createSampleArticleCard(
                title = "The Future of Cross-Platform Development",
                summary = "Cross-platform development has evolved significantly over the years. " +
                    "From early frameworks that tried to abstract everything to modern solutions " +
                    "that embrace platform differences while sharing business logic. " +
                    "This article explores the current state and future trends.",
                url = "https://example.com/article",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 创建示例文章卡片数据
 */
private fun createSampleArticleCard(
    title: String,
    summary: String,
    url: String,
    isFavorite: Boolean = false
): Card {
    val now = kotlin.time.Clock.System.now()
    return Card(
        id = "preview-article-1",
        type = CardType.ARTICLE,
        title = title,
        content = summary,
        isFavorite = isFavorite,
        createdAt = now,
        updatedAt = now,
        metadata = CardMetadata(
            articleUrl = url,
            articleSummary = summary
        )
    )
}

