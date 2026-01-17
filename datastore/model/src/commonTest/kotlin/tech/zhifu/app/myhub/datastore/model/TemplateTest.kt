package tech.zhifu.app.myhub.datastore.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * Template 数据模型单元测试
 */
class TemplateTest {

    @Test
    fun `test Template creation with required fields`() {
        // Given
        val now = Clock.System.now()

        // When
        val template = Template(
            id = "template-1",
            name = "Test Template",
            cardType = CardType.QUOTE,
            createdAt = now,
            updatedAt = now
        )

        // Then
        assertEquals("template-1", template.id)
        assertEquals("Test Template", template.name)
        assertEquals(CardType.QUOTE, template.cardType)
        assertNull(template.description)
        assertNull(template.previewImageUrl)
        assertNull(template.defaultContent)
        assertNull(template.defaultMetadata)
        assertTrue(template.defaultTags.isEmpty())
        assertEquals(0, template.usageCount)
        assertFalse(template.isSystemTemplate)
        assertNull(template.userId)
    }

    @Test
    fun `test Template creation with all fields`() {
        // Given
        val now = Clock.System.now()
        val metadata = CardMetadata(quoteAuthor = "Author")

        // When
        val template = Template(
            id = "template-1",
            name = "Test Template",
            description = "Test description",
            cardType = CardType.QUOTE,
            previewImageUrl = "https://example.com/preview.jpg",
            defaultContent = "Default content",
            defaultMetadata = metadata,
            defaultTags = listOf("tag1", "tag2"),
            usageCount = 10,
            isSystemTemplate = true,
            createdAt = now,
            updatedAt = now,
            userId = "system"
        )

        // Then
        assertEquals("template-1", template.id)
        assertEquals("Test Template", template.name)
        assertEquals("Test description", template.description)
        assertEquals(CardType.QUOTE, template.cardType)
        assertEquals("https://example.com/preview.jpg", template.previewImageUrl)
        assertEquals("Default content", template.defaultContent)
        assertNotNull(template.defaultMetadata)
        assertEquals(2, template.defaultTags.size)
        assertEquals(10, template.usageCount)
        assertTrue(template.isSystemTemplate)
        assertEquals("system", template.userId)
    }
}

