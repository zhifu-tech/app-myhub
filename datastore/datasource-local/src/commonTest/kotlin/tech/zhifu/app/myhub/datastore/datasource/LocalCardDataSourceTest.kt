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

    private val testUserId = "test-user-1"

    @Test
    fun `test insert and get card`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE)

        // When
        dataSource.insertCard(card, testUserId)
        val result = dataSource.getCardById("1", testUserId)

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
        dataSource.insertCard(card1, testUserId)
        dataSource.insertCard(card2, testUserId)
        dataSource.insertCard(card3, testUserId)
        val result = dataSource.getAllCards(testUserId)

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
        dataSource.insertCard(card, testUserId)

        // When
        val updatedCard = card.copy(
            content = "Updated Content",
            title = "Updated Title"
        )
        dataSource.updateCard(updatedCard, testUserId)
        val result = dataSource.getCardById("1", testUserId)

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
        dataSource.insertCard(card, testUserId)

        // When
        dataSource.deleteCard("1", testUserId)
        val result = dataSource.getCardById("1", testUserId)

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
        val flow = dataSource.observeCards(testUserId)
        dataSource.insertCard(card1, testUserId)
        val firstResult = flow.first()

        dataSource.insertCard(card2, testUserId)
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
        dataSource.insertCard(card, testUserId)
        val result = dataSource.getCardById("1", testUserId)

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
        dataSource.insertCard(card, testUserId)
        val result = dataSource.getCardById("1", testUserId)

        // Then
        assertNotNull(result)
        assertNotNull(result.metadata)
        assertEquals("Test Author", result.metadata!!.quoteAuthor)
        assertEquals("Literature", result.metadata!!.quoteCategory)
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
        dataSource.insertCard(card, testUserId)
        val result = dataSource.getCardById("1", testUserId)

        // Then
        assertNotNull(result)
        assertNotNull(result.metadata)
        assertEquals(3, result.metadata!!.checklistItems.size)
        assertEquals("Task 1", result.metadata!!.checklistItems[0].text)
        assertEquals(result.metadata!!.checklistItems[1].isCompleted, true)
    }

    @Test
    fun `test favorite card`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val card = createTestCard("1", CardType.QUOTE).copy(
            isFavorite = true
        )

        // When
        dataSource.insertCard(card, testUserId)
        val result = dataSource.getCardById("1", testUserId)

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
        dataSource.insertCard(card1, testUserId)
        dataSource.insertCard(card2, testUserId)

        // When
        dataSource.deleteAllCards(testUserId)
        val result = dataSource.getAllCards(testUserId)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test multi-user data isolation`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalCardDataSourceImpl(database)
        val userId1 = "user-1"
        val userId2 = "user-2"
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        val card3 = createTestCard("3", CardType.IDEA)

        // When - User 1 creates cards
        dataSource.insertCard(card1, userId1)
        dataSource.insertCard(card2, userId1)

        // User 2 creates a card
        dataSource.insertCard(card3, userId2)

        // Then - User 1 should only see their cards
        val user1Cards = dataSource.getAllCards(userId1)
        assertEquals(2, user1Cards.size)
        assertTrue(user1Cards.any { it.id == "1" })
        assertTrue(user1Cards.any { it.id == "2" })
        assertTrue(user1Cards.none { it.id == "3" })

        // User 2 should only see their card
        val user2Cards = dataSource.getAllCards(userId2)
        assertEquals(1, user2Cards.size)
        assertTrue(user2Cards.any { it.id == "3" })
        assertTrue(user2Cards.none { it.id == "1" })
        assertTrue(user2Cards.none { it.id == "2" })

        // User 1 cannot access User 2's card
        val user1Card3 = dataSource.getCardById("3", userId1)
        assertNull(user1Card3)

        // User 2 cannot access User 1's cards
        val user2Card1 = dataSource.getCardById("1", userId2)
        assertNull(user2Card1)
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

