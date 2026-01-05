package tech.zhifu.app.myhub.component.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers

/**
 * Card 扩展方法单元测试
 */
class CardExtensionsTest {

    @Test
    fun `test getContentPreview with short content`() {
        // Given
        val card = MockCardHelpers.createTestQuoteCard(
            content = "Short content"
        )

        // When
        val preview = card.getContentPreview()

        // Then
        assertEquals("Short content", preview)
    }

    @Test
    fun `test getContentPreview with long content truncates`() {
        // Given
        val longContent = "A".repeat(100)
        val card = MockCardHelpers.createTestQuoteCard(
            content = longContent
        )

        // When
        val preview = card.getContentPreview(maxLength = 80)

        // Then
        assertEquals(83, preview.length) // 80 chars + "..."
        assertTrue(preview.endsWith("..."))
        assertEquals(longContent.take(80) + "...", preview)
    }

    @Test
    fun `test getContentPreview with custom maxLength`() {
        // Given
        val content = "This is a test content that is longer than 20 characters"
        val card = MockCardHelpers.createTestQuoteCard(
            content = content
        )

        // When
        val preview = card.getContentPreview(maxLength = 20)

        // Then
        assertEquals(23, preview.length) // 20 chars + "..."
        assertTrue(preview.endsWith("..."))
        assertEquals(content.take(20) + "...", preview)
    }

    @Test
    fun `test getContentPreview with exact maxLength`() {
        // Given
        val content = "A".repeat(80)
        val card = MockCardHelpers.createTestQuoteCard(
            content = content
        )

        // When
        val preview = card.getContentPreview(maxLength = 80)

        // Then
        assertEquals(content, preview) // 不应该添加 "..."
    }

    @Test
    fun `test getContentPreview with empty content`() {
        // Given
        val card = MockCardHelpers.createTestQuoteCard(
            content = ""
        )

        // When
        val preview = card.getContentPreview()

        // Then
        assertEquals("", preview)
    }
}

