package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * CardDto 转换和序列化测试
 */
class CardDtoTest {

    @Test
    fun `test Card to CardDto conversion`() {
        // Given
        val now = Clock.System.now()
        val card = Card(
            id = "card-1",
            type = CardType.QUOTE,
            title = "Test Title",
            content = "Test content",
            author = "Author",
            tags = listOf("tag1", "tag2"),
            isFavorite = true,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                quoteAuthor = "Quote Author"
            )
        )

        // When
        val dto = card.toDto()

        // Then
        assertEquals("card-1", dto.id)
        assertEquals("quote", dto.type)
        assertEquals("Test Title", dto.title)
        assertEquals("Test content", dto.content)
        assertEquals("Author", dto.author)
        assertEquals(2, dto.tags.size)
        assertTrue(dto.isFavorite)
        assertNotNull(dto.metadata)
        assertEquals("Quote Author", dto.metadata.quoteAuthor)
    }

    @Test
    fun `test CardDto to Card conversion`() {
        // Given
        val now = Clock.System.now().toString()
        val dto = CardDto(
            id = "card-1",
            type = "quote",
            title = "Test Title",
            content = "Test content",
            author = "Author",
            tags = listOf("tag1", "tag2"),
            isFavorite = true,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadataDto(
                quoteAuthor = "Quote Author"
            )
        )

        // When
        val card = dto.toDomain()

        // Then
        assertEquals("card-1", card.id)
        assertEquals(CardType.QUOTE, card.type)
        assertEquals("Test Title", card.title)
        assertEquals("Test content", card.content)
        assertEquals("Author", card.author)
        assertEquals(2, card.tags.size)
        assertTrue(card.isFavorite)
        assertNotNull(card.metadata)
        assertEquals("Quote Author", card.metadata.quoteAuthor)
    }

    @Test
    fun `test CreateCardRequest to Card conversion`() {
        // Given
        val request = CreateCardRequest(
            type = "quote",
            title = "Test Title",
            content = "Test content",
            author = "Author",
            tags = listOf("tag1"),
            isFavorite = true,
            metadata = CardMetadataDto(
                quoteAuthor = "Quote Author"
            )
        )

        // When
        val card = request.toDomain()

        // Then
        assertEquals("", card.id) // 由 Repository 生成
        assertEquals(CardType.QUOTE, card.type)
        assertEquals("Test Title", card.title)
        assertEquals("Test content", card.content)
        assertEquals("Author", card.author)
        assertEquals(1, card.tags.size)
        assertTrue(card.isFavorite)
        assertNotNull(card.metadata)
        assertNotNull(card.createdAt)
        assertNotNull(card.updatedAt)
    }

    @Test
    fun `test ChecklistItem DTO conversion`() {
        // Given
        val item = ChecklistItem(
            id = "item-1",
            text = "Test item",
            isCompleted = true,
            order = 5
        )

        // When
        val dto = item.toDto()
        val converted = dto.toDomain()

        // Then
        assertEquals("item-1", converted.id)
        assertEquals("Test item", converted.text)
        assertTrue(converted.isCompleted)
        assertEquals(5, converted.order)
    }

    @Test
    fun `test Card serialization`() {
        // Given
        val now = Clock.System.now()
        val card = Card(
            id = "card-1",
            type = CardType.QUOTE,
            content = "Test content",
            createdAt = now,
            updatedAt = now
        )

        // When
        val json = Json.encodeToString(card)

        // Then
        assertNotNull(json)
        assertTrue(json.contains("card-1"))
        assertTrue(json.contains("QUOTE"))
    }

    @Test
    fun `test CardDto serialization`() {
        // Given
        val now = Clock.System.now().toString()
        val dto = CardDto(
            id = "card-1",
            type = "quote",
            content = "Test content",
            createdAt = now,
            updatedAt = now
        )

        // When
        val json = Json.encodeToString(dto)

        // Then
        assertNotNull(json)
        assertTrue(json.contains("card-1"))
        assertTrue(json.contains("quote"))
    }
}

