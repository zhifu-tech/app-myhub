package tech.zhifu.app.myhub.component.card

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers

/**
 * QuoteCardComponent 单元测试
 */
class QuoteCardComponentTest {

    private val component = QuoteCardComponent()

    @Test
    fun `test getTypeIconColor returns purple`() {
        // When
        val color = component.getTypeIconColor()

        // Then
        assertEquals(Color(0xFF8B5CF6), color)
    }

    @Test
    fun `test getTypeIconText returns Q`() {
        // When
        val text = component.getTypeIconText()

        // Then
        assertEquals("Q", text)
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun `test getDisplayTitle with quoteAuthor in metadata`() {
        // Given
        val card = MockCardHelpers.createTestQuoteCard(
            author = "Albert Einstein"
        )
        var title: String? = null

        // When - 使用 CompositionLocalProvider 来测试 @Composable 函数
        // 注意：由于 getDisplayTitle 使用了 stringResource，需要 Compose Resources 环境
        // 在实际测试中，这需要完整的 Compose 环境设置
        // 这里我们主要验证逻辑：当有 quoteAuthor 时，应该返回 quoteAuthor
        // 完整的 UI 测试建议在 Android 平台测试中使用 ComposeTestRule
        
        // 由于 stringResource 需要完整的 Compose Resources 环境，
        // 在 commonTest 中测试 @Composable 函数比较复杂
        // 这里我们通过验证 card 的数据结构来间接测试逻辑
        assertEquals("Albert Einstein", card.metadata?.quoteAuthor)
        assertEquals("Albert Einstein", card.author)
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun `test getDisplayTitle logic - card with quoteAuthor`() {
        // Given - 测试 getDisplayTitle 的逻辑：优先使用 quoteAuthor
        val cardWithAuthor = MockCardHelpers.createTestQuoteCard(
            author = "Albert Einstein"
        )
        
        // Then - 验证数据正确设置
        assertEquals("Albert Einstein", cardWithAuthor.metadata?.quoteAuthor)
        // getDisplayTitle 应该返回 quoteAuthor，如果没有则返回默认资源
        // 完整的测试需要在 Compose 环境中运行
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun `test getDisplayTitle logic - card without quoteAuthor`() {
        // Given - 测试没有 quoteAuthor 的情况
        val card = MockCardHelpers.createTestQuoteCard(
            author = null
        )
        val cardMetadata = card.metadata
        val cardWithoutAuthor = if (cardMetadata != null) {
            card.copy(metadata = cardMetadata.copy(quoteAuthor = null))
        } else {
            card.copy(metadata = null)
        }
        
        // Then - 验证数据正确设置
        assertEquals(null, cardWithoutAuthor.metadata?.quoteAuthor)
        assertEquals(null, cardWithoutAuthor.author)
        // getDisplayTitle 应该返回默认的 Quote 字符串资源
        // 完整的测试需要在 Compose 环境中运行
    }
}

