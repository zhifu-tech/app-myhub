package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
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
private fun QuoteCardLightPreview() {
    AppTheme(darkTheme = false) {
        QuoteCard(
            card = createSampleQuoteCard(
                content = "The only way to do great work is to love what you do.",
                author = "Steve Jobs",
                category = "MOTIVATION",
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
private fun QuoteCardDarkPreview() {
    AppTheme(darkTheme = true) {
        QuoteCard(
            card = createSampleQuoteCard(
                content = "The only way to do great work is to love what you do.",
                author = "Steve Jobs",
                category = "MOTIVATION",
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
private fun QuoteCardFavoritePreview() {
    AppTheme(darkTheme = false) {
        QuoteCard(
            card = createSampleQuoteCard(
                content = "Innovation distinguishes between a leader and a follower.",
                author = "Steve Jobs",
                category = "BUSINESS",
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
private fun QuoteCardLongTextPreview() {
    AppTheme(darkTheme = false) {
        QuoteCard(
            card = createSampleQuoteCard(
                content = "Life is what happens to you while you're busy making other plans. " +
                    "The future belongs to those who believe in the beauty of their dreams. " +
                    "It is during our darkest moments that we must focus to see the light.",
                author = "John Lennon",
                category = "LITERATURE",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 创建示例引言卡片数据
 */
private fun createSampleQuoteCard(
    content: String,
    author: String,
    category: String,
    isFavorite: Boolean = false
): Card {
    val now = Clock.System.now()
    return Card(
        id = "preview-quote-1",
        type = CardType.QUOTE,
        content = content,
        author = author,
        isFavorite = isFavorite,
        createdAt = now,
        updatedAt = now,
        metadata = CardMetadata(
            quoteAuthor = author,
            quoteCategory = category
        )
    )
}
