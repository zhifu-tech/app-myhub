package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.ChecklistItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * LocalCardDataSource 测试
 */
class LocalCardDataSourceTest {

    @Test
    fun `test insert and get card`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE)

        // When
        dataSource.insertCard(card)
        val result = dataSource.getCardById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals(CardType.QUOTE, result.type)
        assertEquals("Test Content", result.content)
    }

    @Test
    fun `test get all cards`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        val card3 = createTestCard("3", CardType.IDEA)

        // When
        dataSource.insertCard(card1)
        dataSource.insertCard(card2)
        dataSource.insertCard(card3)
        val result = dataSource.getAllCards()

        // Then
        assertEquals(3, result.size)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
        assertTrue(result.any { it.id == "3" })
    }

    @Test
    fun `test update card`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE)
        dataSource.insertCard(card)

        // When
        val updatedCard = card.copy(
            content = "Updated Content",
            title = "Updated Title"
        )
        dataSource.updateCard(updatedCard)
        val result = dataSource.getCardById("1")

        // Then
        assertNotNull(result)
        assertEquals("Updated Content", result.content)
        assertEquals("Updated Title", result.title)
    }

    @Test
    fun `test delete card`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE)
        dataSource.insertCard(card)

        // When
        dataSource.deleteCard("1")
        val result = dataSource.getCardById("1")

        // Then
        assertNull(result)
    }

    @Test
    fun `test observe cards flow`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)

        // When
        val flow = dataSource.observeCards()
        dataSource.insertCard(card1)
        val firstResult = flow.first()

        dataSource.insertCard(card2)
        val secondResult = flow.first()

        // Then
        assertTrue(firstResult.size >= 1)
        assertTrue(secondResult.size >= 2)
    }

    @Test
    fun `test card with tags`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE).copy(
            tags = listOf("tag1", "tag2", "tag3")
        )

        // When
        dataSource.insertCard(card)
        val result = dataSource.getCardById("1")

        // Then
        assertNotNull(result)
        assertEquals(3, result.tags.size)
        assertTrue(result.tags.contains("tag1"))
        assertTrue(result.tags.contains("tag2"))
        assertTrue(result.tags.contains("tag3"))
    }

    @Test
    fun `test card with metadata`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val metadata = CardMetadata(
            quoteAuthor = "Test Author",
            quoteCategory = "Literature"
        )
        val card = createTestCard("1", CardType.QUOTE).copy(
            metadata = metadata
        )

        // When
        dataSource.insertCard(card)
        val result = dataSource.getCardById("1")

        // Then
        assertNotNull(result)
        assertNotNull(result.metadata)
        assertEquals("Test Author", result.metadata.quoteAuthor)
        assertEquals("Literature", result.metadata.quoteCategory)
    }

    @Test
    fun `test card with checklist items`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val checklistItems = listOf(
            ChecklistItem("item1", "Task 1", false, 0),
            ChecklistItem("item2", "Task 2", true, 1),
            ChecklistItem("item3", "Task 3", false, 2)
        )
        val metadata = CardMetadata(checklistItems = checklistItems)
        val card = createTestCard("1", CardType.CHECKLIST).copy(
            metadata = metadata
        )

        // When
        dataSource.insertCard(card)
        val result = dataSource.getCardById("1")

        // Then
        assertNotNull(result)
        assertNotNull(result.metadata)
        assertEquals(3, result.metadata.checklistItems.size)
        assertEquals("Task 1", result.metadata.checklistItems.get(0).text)
        assertEquals(result.metadata.checklistItems.get(1).isCompleted, true)
    }

    @Test
    fun `test favorite card`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE).copy(
            isFavorite = true
        )

        // When
        dataSource.insertCard(card)
        val result = dataSource.getCardById("1")

        // Then
        assertNotNull(result)
        assertTrue(result.isFavorite)
    }

    @Test
    fun `test delete all cards`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        dataSource.insertCard(card1)
        dataSource.insertCard(card2)

        // When
        dataSource.deleteAllCards()
        val result = dataSource.getAllCards()

        // Then
        assertTrue(result.isEmpty())
    }

    /**
     * 创建测试用的卡片
     */
    private fun createTestCard(
        id: String,
        type: CardType,
        title: String = "Test Title",
        content: String = "Test Content"
    ): Card {
        val now = Clock.System.now()
        return Card(
            id = id,
            type = type,
            title = title,
            content = content,
            author = "Test Author",
            source = "Test Source",
            language = null,
            tags = emptyList(),
            isFavorite = false,
            isTemplate = false,
            createdAt = now,
            updatedAt = now,
            lastReviewedAt = null,
            metadata = null
        )
    }
}
